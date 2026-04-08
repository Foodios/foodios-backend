package vn.com.orchestration.foodios.service.merchant.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantResponse;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.service.merchant.AdminMerchantService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Set;

import static vn.com.orchestration.foodios.constant.ErrorConstant.ADMIN_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_SLUG_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_SLUG_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMerchantServiceImpl implements AdminMerchantService {

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    @Override
    @Transactional
    public CreateMerchantResponse createMerchant(CreateMerchantRequest request) {
        CreateMerchantRequest.CreateMerchantRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        log.info(
                "[CREATE_MERCHANT] requestId={}, merchantName={}, contactEmail={}",
                request.getRequestId(),
                data.getMerchantName(),
                data.getContactEmail()
        );

        authorizeAdmin(request);

        String merchantName = data.getMerchantName().trim();
        String merchantSlug = buildMerchantSlug(merchantName);
        String contactEmail = data.getContactEmail().trim().toLowerCase(Locale.ROOT);

        if (merchantRepository.existsBySlug(merchantSlug)) {
            throw businessException(request, DUPLICATE_ERROR, MERCHANT_SLUG_EXISTS_MESSAGE);
        }
        if (merchantRepository.existsByContactEmailIgnoreCase(contactEmail)) {
            throw businessException(request, DUPLICATE_ERROR, MERCHANT_EXISTS_MESSAGE);
        }
        if (storeRepository.existsBySlug(merchantSlug)) {
            throw businessException(request, DUPLICATE_ERROR, STORE_SLUG_EXISTS_MESSAGE);
        }

        boolean autoVerify = Boolean.TRUE.equals(data.getAutoVerify());
        MerchantStatus merchantStatus = autoVerify ? MerchantStatus.ACTIVE : MerchantStatus.PENDING;
        StoreStatus storeStatus = autoVerify ? StoreStatus.ACTIVE : StoreStatus.DRAFT;

        Merchant merchant = Merchant.builder()
                .displayName(merchantName)
                .legalName(trimToNull(data.getLegalName()))
                .slug(merchantSlug)
                .logoUrl(trimToNull(data.getLogoUrl()))
                .cuisineCategory(trimToNull(data.getCuisineCategory()))
                .contactEmail(contactEmail)
                .supportHotline(data.getSupportHotline().trim())
                .status(merchantStatus)
                .build();
        Merchant savedMerchant = merchantRepository.saveAndFlush(merchant);

        AddressSnapshot addressSnapshot = new AddressSnapshot();
        addressSnapshot.setDistrict(data.getLocationDistrict().trim());

        Store store = Store.builder()
                .merchant(savedMerchant)
                .name(merchantName)
                .slug(merchantSlug)
                .phone(data.getSupportHotline().trim())
                .status(storeStatus)
                .heroImageUrl(trimToNull(data.getLogoUrl()))
                .opensAt(LocalTime.parse(data.getOpeningTime().trim()))
                .closesAt(LocalTime.parse(data.getClosingTime().trim()))
                .address(addressSnapshot)
                .build();
        Store savedStore = storeRepository.saveAndFlush(store);

        CreateMerchantResponse response = new CreateMerchantResponse();
        response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
        response.setData(
                CreateMerchantResponse.CreateMerchantResponseData.builder()
                        .merchantId(savedMerchant.getId())
                        .storeId(savedStore.getId())
                        .merchantName(savedMerchant.getDisplayName())
                        .merchantSlug(savedMerchant.getSlug())
                        .logoUrl(savedMerchant.getLogoUrl())
                        .contactEmail(savedMerchant.getContactEmail())
                        .supportHotline(savedMerchant.getSupportHotline())
                        .locationDistrict(savedStore.getAddress() != null ? savedStore.getAddress().getDistrict() : null)
                        .merchantStatus(savedMerchant.getStatus())
                        .storeStatus(savedStore.getStatus())
                        .build()
        );
        return response;
    }

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw businessException(request, INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }

    private String buildMerchantSlug(String merchantName) {
        String baseSlug = slugify(merchantName);
        String candidate = baseSlug;
        int sequence = 1;
        while (merchantRepository.existsBySlug(candidate) || storeRepository.existsBySlug(candidate)) {
            candidate = baseSlug + "-" + sequence;
            sequence++;
        }
        return candidate;
    }

    private String slugify(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "merchant" : normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
