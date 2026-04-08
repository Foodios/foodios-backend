package vn.com.orchestration.foodios.service.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.store.CreateStoreRequest;
import vn.com.orchestration.foodios.dto.store.CreateStoreResponse;
import vn.com.orchestration.foodios.dto.store.DeleteStoreResponse;
import vn.com.orchestration.foodios.dto.store.GetStoreResponse;
import vn.com.orchestration.foodios.dto.store.GetStoresResponse;
import vn.com.orchestration.foodios.dto.store.StoreAddressPayload;
import vn.com.orchestration.foodios.dto.store.StorePayload;
import vn.com.orchestration.foodios.dto.store.UpdateStoreRequest;
import vn.com.orchestration.foodios.dto.store.UpdateStoreResponse;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.merchant.MerchantStoreService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_SLUG_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class MerchantStoreServiceImpl implements MerchantStoreService {

    private static final String DEFAULT_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private final StoreRepository storeRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional
    public CreateStoreResponse createStore(CreateStoreRequest request) {
        CreateStoreRequest.CreateStoreRequestData data = requireData(request, request.getData());
        Merchant merchant = resolveMerchant(request, data.getMerchantId());
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        String normalizedName = normalizeRequired(data.getName(), "Store name", request);
        String slug = resolveCreateSlug(data.getSlug(), normalizedName);

        Store store = Store.builder()
                .merchant(merchant)
                .name(normalizedName)
                .slug(slug)
                .phone(trimToNull(data.getPhone()))
                .status(data.getStatus() == null ? StoreStatus.DRAFT : data.getStatus())
                .timeZone(resolveTimeZone(data.getTimeZone()))
                .heroImageUrl(trimToNull(data.getHeroImageUrl()))
                .opensAt(data.getOpensAt())
                .closesAt(data.getClosesAt())
                .address(toAddressSnapshot(data.getAddress()))
                .build();

        Store savedStore = storeRepository.saveAndFlush(store);
        return CreateStoreResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedStore))
                .build();
    }

    @Override
    @Transactional
    public UpdateStoreResponse updateStore(UUID storeId, UpdateStoreRequest request) {
        UpdateStoreRequest.UpdateStoreRequestData data = requireData(request, request.getData());
        Store store = resolveStore(request, storeId);
        validateMerchantScope(request, store, data.getMerchantId());

        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store.getMerchant().getId(), currentUser.getId());

        if (data.getName() != null) {
            store.setName(normalizeRequired(data.getName(), "Store name", request));
        }
        if (data.getSlug() != null) {
            store.setSlug(resolveUpdateSlug(data.getSlug(), store.getName(), store.getId(), request));
        } else if (data.getName() != null) {
            store.setSlug(resolveUpdateSlug(null, store.getName(), store.getId(), request));
        }
        if (data.getPhone() != null) {
            store.setPhone(trimToNull(data.getPhone()));
        }
        if (data.getStatus() != null) {
            store.setStatus(data.getStatus());
        }
        if (data.getTimeZone() != null) {
            store.setTimeZone(resolveTimeZone(data.getTimeZone()));
        }
        if (data.getHeroImageUrl() != null) {
            store.setHeroImageUrl(trimToNull(data.getHeroImageUrl()));
        }
        if (data.getOpensAt() != null) {
            store.setOpensAt(data.getOpensAt());
        }
        if (data.getClosesAt() != null) {
            store.setClosesAt(data.getClosesAt());
        }
        if (data.getAddress() != null) {
            applyAddress(store, data.getAddress());
        }

        Store savedStore = storeRepository.saveAndFlush(store);
        return UpdateStoreResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedStore))
                .build();
    }

    @Override
    @Transactional
    public DeleteStoreResponse deleteStore(UUID storeId, BaseRequest request, UUID merchantId) {
        Store store = resolveStore(request, storeId);
        validateMerchantScope(request, store, merchantId);

        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store.getMerchant().getId(), currentUser.getId());

        store.setStatus(StoreStatus.INACTIVE);
        Store savedStore = storeRepository.saveAndFlush(store);

        return DeleteStoreResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedStore))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetStoreResponse getStore(UUID storeId, BaseRequest request, UUID merchantId) {
        Store store = resolveStore(request, storeId);
        validateMerchantScope(request, store, merchantId);

        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, store.getMerchant().getId(), currentUser.getId());

        return GetStoreResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(store))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetStoresResponse getStores(BaseRequest request, UUID merchantId, StoreStatus status) {
        Merchant merchant = resolveMerchant(request, merchantId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        List<Store> stores = status == null
                ? storeRepository.findByMerchantId(merchant.getId()).stream()
                .sorted(Comparator.comparing(Store::getName, String.CASE_INSENSITIVE_ORDER))
                .toList()
                : storeRepository.findByMerchantIdAndStatus(merchant.getId(), status).stream()
                .sorted(Comparator.comparing(Store::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return GetStoresResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetStoresResponse.GetStoresResponseData.builder()
                        .stores(stores.stream().map(this::toPayload).toList())
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

    private Merchant resolveMerchant(BaseRequest request, UUID merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));
    }

    private Store resolveStore(BaseRequest request, UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
    }

    private void authorizeMerchantAccess(BaseRequest request, UUID merchantId, UUID userId) {
        boolean hasAccess = merchantMemberRepository.existsByMerchantIdAndUserIdAndStatusAndRoleIn(
                merchantId,
                userId,
                MerchantMemberStatus.ACTIVE,
                List.of(MerchantMemberRole.OWNER, MerchantMemberRole.MANAGER)
        );
        if (!hasAccess) {
            throw businessException(request, INVALID_INPUT_ERROR, MERCHANT_ACCESS_DENIED_MESSAGE);
        }
    }

    private void validateMerchantScope(BaseRequest request, Store store, UUID merchantId) {
        if (merchantId != null && !store.getMerchant().getId().equals(merchantId)) {
            throw businessException(request, INVALID_INPUT_ERROR, "Store does not belong to merchant");
        }
    }

    private String resolveCreateSlug(String rawSlug, String storeName) {
        String baseSlug = slugify(rawSlug == null || rawSlug.isBlank() ? storeName : rawSlug);
        String candidate = baseSlug;
        int sequence = 1;
        while (storeRepository.existsBySlug(candidate)) {
            candidate = baseSlug + "-" + sequence;
            sequence++;
        }
        return candidate;
    }

    private String resolveUpdateSlug(String rawSlug, String storeName, UUID storeId, BaseRequest request) {
        String candidate = slugify(rawSlug == null || rawSlug.isBlank() ? storeName : rawSlug);
        boolean exists = storeRepository.findBySlug(candidate)
                .filter(store -> !store.getId().equals(storeId))
                .isPresent();
        if (exists) {
            throw businessException(request, DUPLICATE_ERROR, STORE_SLUG_EXISTS_MESSAGE);
        }
        return candidate;
    }

    private String resolveTimeZone(String timeZone) {
        String normalized = trimToNull(timeZone);
        return normalized == null ? DEFAULT_TIME_ZONE : normalized;
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
            return "store";
        }
        String slug = normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "store" : slug;
    }

    private AddressSnapshot toAddressSnapshot(CreateStoreRequest.StoreAddressInput addressInput) {
        AddressSnapshot address = new AddressSnapshot();
        if (addressInput == null) {
            return address;
        }
        address.setContactName(trimToNull(addressInput.getContactName()));
        address.setContactPhone(trimToNull(addressInput.getContactPhone()));
        address.setLine1(trimToNull(addressInput.getLine1()));
        address.setLine2(trimToNull(addressInput.getLine2()));
        address.setWard(trimToNull(addressInput.getWard()));
        address.setDistrict(trimToNull(addressInput.getDistrict()));
        address.setCity(trimToNull(addressInput.getCity()));
        address.setProvince(trimToNull(addressInput.getProvince()));
        address.setPostalCode(trimToNull(addressInput.getPostalCode()));
        address.setCountry(trimToNull(addressInput.getCountry()));
        address.setLatitude(addressInput.getLatitude());
        address.setLongitude(addressInput.getLongitude());
        return address;
    }

    private void applyAddress(Store store, UpdateStoreRequest.StoreAddressInput addressInput) {
        AddressSnapshot address = store.getAddress();
        if (address == null) {
            address = new AddressSnapshot();
            store.setAddress(address);
        }
        if (addressInput.getContactName() != null) {
            address.setContactName(trimToNull(addressInput.getContactName()));
        }
        if (addressInput.getContactPhone() != null) {
            address.setContactPhone(trimToNull(addressInput.getContactPhone()));
        }
        if (addressInput.getLine1() != null) {
            address.setLine1(trimToNull(addressInput.getLine1()));
        }
        if (addressInput.getLine2() != null) {
            address.setLine2(trimToNull(addressInput.getLine2()));
        }
        if (addressInput.getWard() != null) {
            address.setWard(trimToNull(addressInput.getWard()));
        }
        if (addressInput.getDistrict() != null) {
            address.setDistrict(trimToNull(addressInput.getDistrict()));
        }
        if (addressInput.getCity() != null) {
            address.setCity(trimToNull(addressInput.getCity()));
        }
        if (addressInput.getProvince() != null) {
            address.setProvince(trimToNull(addressInput.getProvince()));
        }
        if (addressInput.getPostalCode() != null) {
            address.setPostalCode(trimToNull(addressInput.getPostalCode()));
        }
        if (addressInput.getCountry() != null) {
            address.setCountry(trimToNull(addressInput.getCountry()));
        }
        if (addressInput.getLatitude() != null) {
            address.setLatitude(addressInput.getLatitude());
        }
        if (addressInput.getLongitude() != null) {
            address.setLongitude(addressInput.getLongitude());
        }
    }

    private StorePayload toPayload(Store store) {
        return StorePayload.builder()
                .id(store.getId())
                .merchantId(store.getMerchant().getId())
                .name(store.getName())
                .slug(store.getSlug())
                .phone(store.getPhone())
                .status(store.getStatus())
                .timeZone(store.getTimeZone())
                .heroImageUrl(store.getHeroImageUrl())
                .opensAt(store.getOpensAt())
                .closesAt(store.getClosesAt())
                .address(toAddressPayload(store.getAddress()))
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    private StoreAddressPayload toAddressPayload(AddressSnapshot address) {
        if (address == null) {
            return null;
        }
        return StoreAddressPayload.builder()
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .province(address.getProvince())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
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
