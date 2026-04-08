package vn.com.orchestration.foodios.service.catalog.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductResponse;
import vn.com.orchestration.foodios.dto.product.DeleteProductResponse;
import vn.com.orchestration.foodios.dto.product.GetProductResponse;
import vn.com.orchestration.foodios.dto.product.GetProductsResponse;
import vn.com.orchestration.foodios.dto.product.ProductPayload;
import vn.com.orchestration.foodios.dto.product.UpdateProductRequest;
import vn.com.orchestration.foodios.dto.product.UpdateProductResponse;
import vn.com.orchestration.foodios.entity.catalog.Category;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.repository.CategoryRepository;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.catalog.MerchantProductService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.CATEGORY_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.PRODUCT_SKU_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantProductServiceImpl implements MerchantProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        CreateProductRequest.CreateProductRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        sLog.info(
                "[CREATE_PRODUCT] requestId={}, storeId={}, categoryId={}, name={}",
                request.getRequestId(),
                data.getStoreId(),
                data.getCategoryId(),
                data.getName()
        );

        IdentityUserContext currentUserContext = identityUserContextProvider.requireCurrentUser();
        User currentUser = resolveCurrentUser(request, currentUserContext);

        Store store = storeRepository.findById(data.getStoreId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));

        authorizeMerchantAccess(request, store, currentUser.getId());

        Category category = resolveCategory(request, data.getCategoryId(), store.getId());
        String normalizedName = normalizeRequired(data.getName(), "Product name", request);
        String slug = resolveCreateSlug(store.getId(), data.getSlug(), normalizedName, request);
        String sku = resolveSku(data.getSku(), store.getId(), slug, request);

        Product product = Product.builder()
                .store(store)
                .category(category)
                .name(normalizedName)
                .slug(slug)
                .description(trimToNull(data.getDescription()))
                .price(data.getPrice())
                .compareAtPrice(data.getCompareAtPrice())
                .currency(resolveCurrency(data.getCurrency()))
                .sku(sku)
                .imageUrl(trimToNull(data.getImageUrl()))
                .stockQuantity(resolveInternalStock(data.getInternalStock()))
                .sortOrder(data.getSortOrder() == null ? 0 : data.getSortOrder())
                .featured(Boolean.TRUE.equals(data.getFeatured()))
                .status(resolveStatus(data.getStatus()))
                .available(resolveAvailable(data.getAvailable(), data.getInternalStock()))
                .preparationTimeMinutes(data.getPreparationTimeMinutes())
                .build();

        Product savedProduct = productRepository.saveAndFlush(product);

        return CreateProductResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedProduct))
                .build();
    }

    @Override
    @Transactional
    public UpdateProductResponse updateProduct(UUID productId, UpdateProductRequest request) {
        UpdateProductRequest.UpdateProductRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        IdentityUserContext currentUserContext = identityUserContextProvider.requireCurrentUser();
        User currentUser = resolveCurrentUser(request, currentUserContext);
        Store store = storeRepository.findById(data.getStoreId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
        authorizeMerchantAccess(request, store, currentUser.getId());

        Product product = resolveProduct(request, productId, store.getId());
        Category category = data.getCategoryId() == null
                ? product.getCategory()
                : resolveCategory(request, data.getCategoryId(), store.getId());

        if (data.getName() != null) {
            product.setName(normalizeRequired(data.getName(), "Product name", request));
        }
        if (data.getSlug() != null) {
            product.setSlug(resolveUpdateSlug(store.getId(), data.getSlug(), product.getName(), product.getId(), request));
        } else if (data.getName() != null) {
            product.setSlug(resolveUpdateSlug(store.getId(), null, product.getName(), product.getId(), request));
        }
        if (data.getCategoryId() != null) {
            product.setCategory(category);
        }
        if (data.getDescription() != null) {
            product.setDescription(trimToNull(data.getDescription()));
        }
        if (data.getPrice() != null) {
            product.setPrice(data.getPrice());
        }
        if (data.getCompareAtPrice() != null) {
            product.setCompareAtPrice(data.getCompareAtPrice());
        }
        if (data.getCurrency() != null) {
            product.setCurrency(resolveCurrency(data.getCurrency()));
        }
        if (data.getSku() != null) {
            product.setSku(resolveUpdateSku(data.getSku(), store.getId(), product.getSlug(), product.getId(), request));
        }
        if (data.getImageUrl() != null) {
            product.setImageUrl(trimToNull(data.getImageUrl()));
        }
        if (data.getInternalStock() != null) {
            product.setStockQuantity(resolveInternalStock(data.getInternalStock()));
            if (data.getAvailable() == null) {
                product.setAvailable(resolveInternalStock(data.getInternalStock()) > 0);
            }
        }
        if (data.getSortOrder() != null) {
            product.setSortOrder(data.getSortOrder());
        }
        if (data.getFeatured() != null) {
            product.setFeatured(data.getFeatured());
        }
        if (data.getAvailable() != null) {
            product.setAvailable(data.getAvailable());
        }
        if (data.getPreparationTimeMinutes() != null) {
            product.setPreparationTimeMinutes(data.getPreparationTimeMinutes());
        }
        if (data.getStatus() != null) {
            product.setStatus(data.getStatus());
        }

        Product savedProduct = productRepository.saveAndFlush(product);
        return UpdateProductResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedProduct))
                .build();
    }

    @Override
    @Transactional
    public DeleteProductResponse deleteProduct(UUID productId, BaseRequest request, UUID storeId) {
        IdentityUserContext currentUserContext = identityUserContextProvider.requireCurrentUser();
        User currentUser = resolveCurrentUser(request, currentUserContext);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
        authorizeMerchantAccess(request, store, currentUser.getId());

        Product product = resolveProduct(request, productId, store.getId());
        product.setStatus(ProductStatus.INACTIVE);
        product.setAvailable(false);

        Product savedProduct = productRepository.saveAndFlush(product);
        return DeleteProductResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedProduct))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetProductResponse getProduct(UUID productId, BaseRequest request, UUID storeId) {
        IdentityUserContext currentUserContext = identityUserContextProvider.requireCurrentUser();
        User currentUser = resolveCurrentUser(request, currentUserContext);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
        authorizeMerchantAccess(request, store, currentUser.getId());

        Product product = resolveProduct(request, productId, store.getId());
        return GetProductResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(product))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetProductsResponse getProducts(BaseRequest request, UUID storeId, UUID categoryId, ProductStatus status) {
        IdentityUserContext currentUserContext = identityUserContextProvider.requireCurrentUser();
        User currentUser = resolveCurrentUser(request, currentUserContext);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
        authorizeMerchantAccess(request, store, currentUser.getId());

        if (categoryId != null) {
            resolveCategory(request, categoryId, store.getId());
        }

        List<Product> products;
        if (categoryId != null && status != null) {
            products = productRepository.findByStoreIdAndCategoryIdAndStatus(
                    store.getId(),
                    categoryId,
                    status,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
        } else if (categoryId != null) {
            products = productRepository.findByStoreIdAndCategoryId(
                    store.getId(),
                    categoryId,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
        } else if (status != null) {
            products = productRepository.findByStoreIdAndStatusOrderBySortOrderAscNameAsc(store.getId(), status);
        } else {
            products = productRepository.findByStoreId(store.getId()).stream()
                    .sorted(Comparator.comparingInt(Product::getSortOrder).thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }

        return GetProductsResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetProductsResponse.GetProductsResponseData.builder()
                        .products(products.stream().map(this::toPayload).toList())
                        .build())
                .build();
    }

    private User resolveCurrentUser(BaseRequest request, IdentityUserContext userContext) {
        if (userContext.email() != null && !userContext.email().isBlank()) {
            return userRepository.findByEmail(userContext.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        UUID userId = UUID.fromString(userContext.subject());
        return userRepository.findById(userId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
    }

    private void authorizeMerchantAccess(BaseRequest request, Store store, UUID userId) {
        boolean hasAccess = merchantMemberRepository.existsByMerchantIdAndUserIdAndStatusAndRoleIn(
                store.getMerchant().getId(),
                userId,
                MerchantMemberStatus.ACTIVE,
                List.of(MerchantMemberRole.OWNER, MerchantMemberRole.MANAGER)
        );
        if (!hasAccess) {
            throw businessException(request, INVALID_INPUT_ERROR, MERCHANT_ACCESS_DENIED_MESSAGE);
        }
    }

    private Category resolveCategory(BaseRequest request, UUID categoryId, UUID storeId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getStore().getId().equals(storeId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, CATEGORY_NOT_FOUND_MESSAGE));
    }

    private Product resolveProduct(BaseRequest request, UUID productId, UUID storeId) {
        return productRepository.findById(productId)
                .filter(product -> product.getStore().getId().equals(storeId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Product not found"));
    }

    private String resolveCreateSlug(UUID storeId, String rawSlug, String productName, BaseRequest request) {
        String baseSlug = slugify(rawSlug == null || rawSlug.isBlank() ? productName : rawSlug);
        String candidate = baseSlug;
        int sequence = 1;
        while (productRepository.existsByStoreIdAndSlug(storeId, candidate)) {
            candidate = baseSlug + "-" + sequence;
            sequence++;
        }
        return candidate;
    }

    private String resolveUpdateSlug(UUID storeId, String rawSlug, String productName, UUID productId, BaseRequest request) {
        String candidate = slugify(rawSlug == null || rawSlug.isBlank() ? productName : rawSlug);
        if (productRepository.existsByStoreIdAndSlugAndIdNot(storeId, candidate, productId)) {
            throw businessException(request, DUPLICATE_ERROR, "Product slug already exists in store");
        }
        return candidate;
    }

    private String resolveSku(String rawSku, UUID storeId, String slug, BaseRequest request) {
        if (rawSku == null || rawSku.isBlank()) {
            return "SKU-" + slug.toUpperCase(Locale.ROOT).replace('-', '_');
        }

        String sku = rawSku.trim().toUpperCase(Locale.ROOT);
        if (productRepository.existsByStoreIdAndSku(storeId, sku)) {
            throw businessException(request, DUPLICATE_ERROR, PRODUCT_SKU_EXISTS_MESSAGE);
        }
        return sku;
    }

    private String resolveUpdateSku(String rawSku, UUID storeId, String slug, UUID productId, BaseRequest request) {
        if (rawSku == null || rawSku.isBlank()) {
            return "SKU-" + slug.toUpperCase(Locale.ROOT).replace('-', '_');
        }

        String sku = rawSku.trim().toUpperCase(Locale.ROOT);
        if (productRepository.existsByStoreIdAndSkuAndIdNot(storeId, sku, productId)) {
            throw businessException(request, DUPLICATE_ERROR, PRODUCT_SKU_EXISTS_MESSAGE);
        }
        return sku;
    }

    private String resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "VND";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private int resolveInternalStock(Integer internalStock) {
        return internalStock == null ? 0 : internalStock;
    }

    private boolean resolveAvailable(Boolean available, Integer internalStock) {
        if (available != null) {
            return available;
        }
        return resolveInternalStock(internalStock) > 0;
    }

    private ProductStatus resolveStatus(ProductStatus status) {
        return status == null ? ProductStatus.DRAFT : status;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeRequired(String value, String fieldName, BaseRequest request) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw businessException(request, INVALID_INPUT_ERROR, fieldName + " is required");
        }
        return normalized;
    }

    private String slugify(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "product" : normalized;
    }

    private ProductPayload toPayload(Product product) {
        return ProductPayload.builder()
                .id(product.getId())
                .storeId(product.getStore().getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .currency(product.getCurrency())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .internalStock(product.getStockQuantity())
                .sortOrder(product.getSortOrder())
                .featured(product.isFeatured())
                .available(product.isAvailable())
                .preparationTimeMinutes(product.getPreparationTimeMinutes())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
