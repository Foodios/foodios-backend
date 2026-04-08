package vn.com.orchestration.foodios.service.catalog.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.category.CategoryPayload;
import vn.com.orchestration.foodios.dto.category.CreateCategoryRequest;
import vn.com.orchestration.foodios.dto.category.CreateCategoryResponse;
import vn.com.orchestration.foodios.dto.category.DeleteCategoryResponse;
import vn.com.orchestration.foodios.dto.category.GetCategoriesResponse;
import vn.com.orchestration.foodios.dto.category.GetCategoryResponse;
import vn.com.orchestration.foodios.dto.category.UpdateCategoryRequest;
import vn.com.orchestration.foodios.dto.category.UpdateCategoryResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.catalog.Category;
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.CategoryRepository;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.catalog.MerchantCategoryService;
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
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class MerchantCategoryServiceImpl implements MerchantCategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional
    public CreateCategoryResponse createCategory(CreateCategoryRequest request) {
        CreateCategoryRequest.CreateCategoryRequestData data = requireData(request, request.getData());
        Store store = resolveStore(request, data.getStoreId());
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store, currentUser.getId());

        Category parent = resolveParent(request, store.getId(), data.getParentId(), null);
        String normalizedName = normalizeRequired(data.getName(), "Category name", request);
        String resolvedSlug = resolveCreateSlug(request, store.getId(), data.getSlug(), normalizedName);

        Category category = Category.builder()
                .store(store)
                .parent(parent)
                .name(normalizedName)
                .slug(resolvedSlug)
                .description(trimToNull(data.getDescription()))
                .imageUrl(trimToNull(data.getImageUrl()))
                .sortOrder(data.getSortOrder() == null ? 0 : data.getSortOrder())
                .status(data.getStatus() == null ? CategoryStatus.DRAFT : data.getStatus())
                .active(data.getActive() == null || data.getActive())
                .build();

        Category savedCategory = categoryRepository.saveAndFlush(category);

        return CreateCategoryResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedCategory))
                .build();
    }

    @Override
    @Transactional
    public UpdateCategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        UpdateCategoryRequest.UpdateCategoryRequestData data = requireData(request, request.getData());
        Store store = resolveStore(request, data.getStoreId());
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store, currentUser.getId());

        Category category = resolveCategory(request, categoryId, store.getId());
        Category parent = resolveParent(request, store.getId(), data.getParentId(), categoryId);

        if (data.getName() != null) {
            category.setName(normalizeRequired(data.getName(), "Category name", request));
        }
        if (data.getSlug() != null) {
            category.setSlug(resolveUpdateSlug(request, store.getId(), data.getSlug(), category.getName(), category.getId()));
        } else if (data.getName() != null) {
            category.setSlug(resolveUpdateSlug(request, store.getId(), null, category.getName(), category.getId()));
        }
        if (data.getDescription() != null) {
            category.setDescription(trimToNull(data.getDescription()));
        }
        if (data.getImageUrl() != null) {
            category.setImageUrl(trimToNull(data.getImageUrl()));
        }
        if (data.getSortOrder() != null) {
            category.setSortOrder(data.getSortOrder());
        }
        if (data.getStatus() != null) {
            category.setStatus(data.getStatus());
        }
        if (data.getActive() != null) {
            category.setActive(data.getActive());
        }
        category.setParent(parent);

        Category savedCategory = categoryRepository.saveAndFlush(category);
        return UpdateCategoryResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedCategory))
                .build();
    }

    @Override
    @Transactional
    public DeleteCategoryResponse deleteCategory(UUID categoryId, BaseRequest request, UUID storeId) {
        Store store = resolveStore(request, storeId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store, currentUser.getId());

        Category category = resolveCategory(request, categoryId, store.getId());
        if (categoryRepository.existsByParentId(category.getId())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Category has child categories");
        }
        if (productRepository.existsByCategoryId(category.getId())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Category is being used by products");
        }

        category.setActive(false);
        category.setStatus(CategoryStatus.ARCHIVED);
        Category savedCategory = categoryRepository.saveAndFlush(category);

        return DeleteCategoryResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedCategory))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCategoryResponse getCategory(UUID categoryId, BaseRequest request, UUID storeId) {
        Store store = resolveStore(request, storeId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store, currentUser.getId());

        Category category = resolveCategory(request, categoryId, store.getId());
        return GetCategoryResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(category))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCategoriesResponse getCategories(BaseRequest request, UUID storeId, CategoryStatus status, boolean activeOnly) {
        Store store = resolveStore(request, storeId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store, currentUser.getId());

        List<Category> categories;
        if (activeOnly) {
            categories = categoryRepository.findByStoreIdAndActiveTrueOrderBySortOrderAsc(store.getId());
        } else if (status != null) {
            categories = categoryRepository.findByStoreIdAndStatusOrderBySortOrderAsc(store.getId(), status);
        } else {
            categories = categoryRepository.findByStoreId(store.getId()).stream()
                    .sorted(Comparator.comparingInt(Category::getSortOrder).thenComparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }

        return GetCategoriesResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetCategoriesResponse.GetCategoriesResponseData.builder()
                        .categories(categories.stream().map(this::toPayload).toList())
                        .build())
                .build();
    }

    private <T> T requireData(BaseRequest request, T data) {
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }
        return data;
    }

    private User resolveCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isBlank()) {
            return userRepository.findByEmail(currentUser.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        try {
            UUID userId = UUID.fromString(currentUser.subject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        } catch (IllegalArgumentException exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid current user");
        }
    }

    private Store resolveStore(BaseRequest request, UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
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
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getStore().getId().equals(storeId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, CATEGORY_NOT_FOUND_MESSAGE));
    }

    private Category resolveParent(BaseRequest request, UUID storeId, UUID parentId, UUID categoryId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(categoryId)) {
            throw businessException(request, INVALID_INPUT_ERROR, "Category parent cannot be itself");
        }
        return categoryRepository.findById(parentId)
                .filter(category -> category.getStore().getId().equals(storeId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Parent category not found"));
    }

    private String resolveCreateSlug(BaseRequest request, UUID storeId, String rawSlug, String name) {
        String baseSlug = slugify(rawSlug == null || rawSlug.isBlank() ? name : rawSlug);
        String candidate = baseSlug;
        int sequence = 1;
        while (categoryRepository.existsByStoreIdAndSlug(storeId, candidate)) {
            candidate = baseSlug + "-" + sequence;
            sequence++;
        }
        return candidate;
    }

    private String resolveUpdateSlug(BaseRequest request, UUID storeId, String rawSlug, String name, UUID categoryId) {
        String candidate = slugify(rawSlug == null || rawSlug.isBlank() ? name : rawSlug);
        if (categoryRepository.existsByStoreIdAndSlugAndIdNot(storeId, candidate, categoryId)) {
            throw businessException(request, DUPLICATE_ERROR, "Category slug already exists in store");
        }
        return candidate;
    }

    private String normalizeRequired(String value, String fieldName, BaseRequest request) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw businessException(request, INVALID_INPUT_ERROR, fieldName + " is required");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String slugify(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return "category";
        }
        String slug = normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "category" : slug;
    }

    private CategoryPayload toPayload(Category category) {
        return CategoryPayload.builder()
                .id(category.getId())
                .storeId(category.getStore().getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .status(category.getStatus())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
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
