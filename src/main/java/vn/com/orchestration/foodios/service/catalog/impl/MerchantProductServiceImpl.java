package vn.com.orchestration.foodios.service.catalog.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductResponse;
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
import vn.com.orchestration.foodios.utils.ExceptionUtils;

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
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;
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
        String normalizedName = data.getName().trim();
        String slug = buildUniqueSlug(store.getId(), normalizedName);
        String sku = resolveSku(data.getSku(), store.getId(), slug, request);

        Product product = Product.builder()
                .store(store)
                .category(category)
                .name(normalizedName)
                .slug(slug)
                .description(trimToNull(data.getDescription()))
                .price(data.getPrice())
                .currency(resolveCurrency(data.getCurrency()))
                .sku(sku)
                .imageUrl(trimToNull(data.getImageUrl()))
                .stockQuantity(resolveInternalStock(data.getInternalStock()))
                .status(resolveStatus(data.getStatus()))
                .available(resolveInternalStock(data.getInternalStock()) > 0)
                .build();

        Product savedProduct = productRepository.saveAndFlush(product);

        CreateProductResponse response = new CreateProductResponse();
        response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
        response.setData(
                CreateProductResponse.CreateProductResponseData.builder()
                        .id(savedProduct.getId())
                        .storeId(savedProduct.getStore().getId())
                        .categoryId(savedProduct.getCategory() != null ? savedProduct.getCategory().getId() : null)
                        .name(savedProduct.getName())
                        .slug(savedProduct.getSlug())
                        .description(savedProduct.getDescription())
                        .price(savedProduct.getPrice())
                        .currency(savedProduct.getCurrency())
                        .sku(savedProduct.getSku())
                        .imageUrl(savedProduct.getImageUrl())
                        .internalStock(savedProduct.getStockQuantity())
                        .status(savedProduct.getStatus())
                        .build()
        );
        return response;
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

    private String buildUniqueSlug(UUID storeId, String productName) {
        String baseSlug = slugify(productName);
        String candidate = baseSlug;
        int sequence = 1;
        while (productRepository.existsByStoreIdAndSlug(storeId, candidate)) {
            candidate = baseSlug + "-" + sequence;
            sequence++;
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

    private String resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "VND";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private int resolveInternalStock(Integer internalStock) {
        return internalStock == null ? 0 : internalStock;
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

    private String slugify(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "product" : normalized;
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
