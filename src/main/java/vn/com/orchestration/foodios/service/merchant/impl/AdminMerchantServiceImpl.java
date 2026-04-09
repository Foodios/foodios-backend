package vn.com.orchestration.foodios.service.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.ApproveMerchantApplicationRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.DeleteMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantApplicationFormDetailResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantApplicationFormsResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantDetailResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantsResponse;
import vn.com.orchestration.foodios.dto.merchant.RejectMerchantApplicationRequest;
import vn.com.orchestration.foodios.dto.merchant.ReviewMerchantApplicationResponse;
import vn.com.orchestration.foodios.dto.merchant.UpdateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.UpdateMerchantResponse;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.merchant.ApplicationFormStatus;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantApplicationForm;
import vn.com.orchestration.foodios.entity.merchant.MerchantApplicationFormRepository;
import vn.com.orchestration.foodios.entity.merchant.MerchantMember;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.merchant.MerchantPayout;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.OrderRepository;
import vn.com.orchestration.foodios.repository.ReviewRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.auth.UserRoleService;
import vn.com.orchestration.foodios.service.merchant.AdminMerchantService;
import vn.com.orchestration.foodios.service.search.SearchSyncService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.ADMIN_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_NUMBER;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_SIZE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_SLUG_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_SLUG_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class AdminMerchantServiceImpl implements AdminMerchantService {

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");
    private static final String MERCHANT_OWNER_ROLE = "MERCHANT_OWNER";

    private final MerchantRepository merchantRepository;
    private final MerchantApplicationFormRepository merchantApplicationFormRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final UserRoleService userRoleService;
    private final SearchSyncService searchSyncService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    @Transactional
    public CreateMerchantResponse createMerchant(CreateMerchantRequest request) {
        CreateMerchantRequest.CreateMerchantRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        sLog.info(
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
                .commissionRate(data.getCommissionRate() != null ? data.getCommissionRate() : new java.math.BigDecimal("15.00"))
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
        searchSyncService.syncStore(savedStore);

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
                        .commissionRate(savedMerchant.getCommissionRate())
                        .build()
        );
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantsResponse getMerchants(BaseRequest request, String query, Integer pageNumber, Integer pageSize) {
        authorizeAdmin(request);
        validatePagination(request, pageNumber, pageSize);
        String keyword = query == null ? "" : query.trim();

        PageRequest pageable = PageRequest.of(
                pageNumber - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Merchant> merchants = keyword.isEmpty()
                ? merchantRepository.findAll(pageable)
                : merchantRepository.findByDisplayNameContainingIgnoreCase(keyword, pageable);

        return GetMerchantsResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetMerchantsResponse.GetMerchantsResponseData.builder()
                        .items(merchants.getContent().stream().map(this::mapMerchantItem).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(merchants.getTotalElements())
                        .totalPages(merchants.getTotalPages())
                        .hasNext(merchants.hasNext())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantDetailResponse getMerchantDetail(BaseRequest request, UUID id) {
        authorizeAdmin(request);
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));

        // Calculate Metrics
        var reviews = reviewRepository.findByStoreMerchantIdAndStatus(id, ReviewStatus.PUBLISHED);
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        long totalReviews = reviews.size();

        var orders = orderRepository.findByStoreMerchantId(id, Pageable.unpaged()).getContent();
        long totalOrders = orders.size();

        OffsetDateTime startOfMonth = OffsetDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal mtdRevenue = orders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth) || o.getCreatedAt().isEqual(startOfMonth))
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(FoodOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return GetMerchantDetailResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetMerchantDetailResponse.GetMerchantDetailResponseData.builder()
                        .id(merchant.getId())
                        .displayName(merchant.getDisplayName())
                        .legalName(merchant.getLegalName())
                        .description(merchant.getDescription())
                        .taxCode(merchant.getTaxCode())
                        .businessRegistrationNumber(merchant.getBusinessRegistrationNumber())
                        .businessLicenseImageUrl(merchant.getBusinessLicenseImageUrl())
                        .foodSafetyLicenseImageUrl(merchant.getFoodSafetyLicenseImageUrl())
                        .slug(merchant.getSlug())
                        .logoUrl(merchant.getLogoUrl())
                        .cuisineCategory(merchant.getCuisineCategory())
                        .contactEmail(merchant.getContactEmail())
                        .supportHotline(merchant.getSupportHotline())
                        .status(merchant.getStatus().name())
                        .commissionRate(merchant.getCommissionRate())
                        .rating(avgRating)
                        .totalReviews(totalReviews)
                        .totalOrders(totalOrders)
                        .mtdRevenue(mtdRevenue)
                        .createdAt(merchant.getCreatedAt())
                        .updatedAt(merchant.getUpdatedAt())
                        .build())
                .build();
    }

    private GetMerchantsResponse.MerchantPayload mapMerchantItem(Merchant merchant) {
        return GetMerchantsResponse.MerchantPayload.builder()
                .id(merchant.getId().toString())
                .displayName(merchant.getDisplayName())
                .legalName(merchant.getLegalName())
                .slug(merchant.getSlug())
                .logoUrl(merchant.getLogoUrl())
                .description(merchant.getDescription())
                .cuisineCategory(merchant.getCuisineCategory())
                .contactEmail(merchant.getContactEmail())
                .supportHotline(merchant.getSupportHotline())
                .status(merchant.getStatus().name())
                .commissionRate(merchant.getCommissionRate())
                .createdAt(merchant.getCreatedAt() != null ? merchant.getCreatedAt().toString() : null)
                .build();
    }

    @Override
    @Transactional
    public UpdateMerchantResponse updateMerchant(UpdateMerchantRequest request) {
        authorizeAdmin(request);
        var data = request.getData();
        UUID id = UUID.fromString(data.getId());
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));

        if (data.getDisplayName() != null) merchant.setDisplayName(data.getDisplayName());
        if (data.getLegalName() != null) merchant.setLegalName(data.getLegalName());
        if (data.getDescription() != null) merchant.setDescription(data.getDescription());
        if (data.getSlug() != null) {
            String newSlug = slugify(data.getSlug());
            if (!newSlug.equals(merchant.getSlug()) && merchantRepository.existsBySlug(newSlug)) {
                throw businessException(request, DUPLICATE_ERROR, MERCHANT_SLUG_EXISTS_MESSAGE);
            }
            merchant.setSlug(newSlug);
        }
        if (data.getLogoUrl() != null) merchant.setLogoUrl(data.getLogoUrl());
        if (data.getCuisineCategory() != null) merchant.setCuisineCategory(data.getCuisineCategory());
        if (data.getContactEmail() != null) merchant.setContactEmail(data.getContactEmail());
        if (data.getSupportHotline() != null) merchant.setSupportHotline(data.getSupportHotline());
        if (data.getCommissionRate() != null) merchant.setCommissionRate(data.getCommissionRate());
        if (data.getStatus() != null) {
            try {
                merchant.setStatus(MerchantStatus.valueOf(data.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw businessException(request, INVALID_INPUT_ERROR, "Invalid status");
            }
        }

        merchantRepository.save(merchant);
        storeRepository.findByMerchantId(merchant.getId()).forEach(searchSyncService::syncStore);

        return UpdateMerchantResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(UpdateMerchantResponse.UpdateMerchantResponseData.builder()
                        .id(merchant.getId().toString())
                        .status(merchant.getStatus().name())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public DeleteMerchantResponse deleteMerchant(BaseRequest request, UUID id) {
        authorizeAdmin(request);
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));

        merchantRepository.delete(merchant);

        return DeleteMerchantResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(DeleteMerchantResponse.DeleteMerchantResponseData.builder()
                        .id(id.toString())
                        .status("DELETED")
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantApplicationFormsResponse getMerchantApplicationForms(
            BaseRequest request,
            String status,
            Integer pageNumber,
            Integer pageSize) {
        authorizeAdmin(request);
        validatePagination(request, pageNumber, pageSize);

        ApplicationFormStatus applicationStatus = parseStatus(request, status);
        PageRequest pageable = PageRequest.of(
                pageNumber - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "submittedAt", "createdAt")
        );

        Page<MerchantApplicationForm> forms = applicationStatus == null
                ? merchantApplicationFormRepository.findAll(pageable)
                : merchantApplicationFormRepository.findAllByStatus(applicationStatus, pageable);

        return GetMerchantApplicationFormsResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetMerchantApplicationFormsResponse.GetMerchantApplicationFormsResponseData.builder()
                        .items(forms.getContent().stream().map(this::mapApplicationFormItem).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(forms.getTotalElements())
                        .totalPages(forms.getTotalPages())
                        .hasNext(forms.hasNext())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantApplicationFormDetailResponse getMerchantApplicationFormDetail(BaseRequest request, UUID id) {
        authorizeAdmin(request);

        MerchantApplicationForm form = requireApplicationForm(request, id);

        return GetMerchantApplicationFormDetailResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetMerchantApplicationFormDetailResponse.GetMerchantApplicationFormDetailResponseData.builder()
                        .id(form.getId())
                        .formCode(form.getFormCode())
                        .merchantId(form.getMerchantId())
                        .submittedBy(form.getSubmittedBy())
                        .legalName(form.getLegalName())
                        .status(form.getStatus() != null ? form.getStatus().name() : null)
                        .submittedAt(form.getSubmittedAt())
                        .approvedAt(form.getApprovedAt())
                        .approvedBy(form.getApprovedBy())
                        .rejectedBy(form.getRejectedBy())
                        .rejectionReason(form.getRejectionReason())
                        .ownerFullName(form.getOwnerFullName())
                        .ownerEmail(form.getOwnerEmail())
                        .ownerPhone(form.getOwnerPhone())
                        .merchantName(form.getMerchantName())
                        .displayName(form.getDisplayName())
                        .slug(form.getSlug())
                        .description(form.getDescription())
                        .taxCode(form.getTaxCode())
                        .businessRegistrationNumber(form.getBusinessRegistrationNumber())
                        .businessLicenseImageUrl(form.getBusinessLicenseImageUrl())
                        .foodSafetyLicenseImageUrl(form.getFoodSafetyLicenseImageUrl())
                        .contactPhone(form.getContactPhone())
                        .contactEmail(form.getContactEmail())
                        .contactName(form.getContactName())
                        .line1(form.getLine1())
                        .line2(form.getLine2())
                        .district(form.getDistrict())
                        .city(form.getCity())
                        .province(form.getProvince())
                        .postalCode(form.getPostalCode())
                        .country(form.getCountry())
                        .bankName(form.getBankName())
                        .bankAccountName(form.getBankAccountName())
                        .bankAccountNumber(form.getBankAccountNumber())
                        .bankBranch(form.getBankBranch())
                        .createdAt(form.getCreatedAt())
                        .updatedAt(form.getUpdatedAt())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public ReviewMerchantApplicationResponse approveMerchantApplication(ApproveMerchantApplicationRequest request) {
        authorizeAdmin(request);

        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        UUID id = request.getData().getId();
        MerchantApplicationForm form = requireReviewableApplicationForm(request, id);
        validateMerchantUniqueness(request, form);
        String displayName = resolveDisplayName(request, form);

        Merchant merchant = Merchant.builder()
                .displayName(displayName)
                .legalName(trimToNull(form.getLegalName()))
                .description(trimToNull(form.getDescription()))
                .taxCode(trimToNull(form.getTaxCode()))
                .businessRegistrationNumber(trimToNull(form.getBusinessRegistrationNumber()))
                .businessLicenseImageUrl(trimToNull(form.getBusinessLicenseImageUrl()))
                .foodSafetyLicenseImageUrl(trimToNull(form.getFoodSafetyLicenseImageUrl()))
                .merchantPayout(MerchantPayout.builder()
                        .bankName(trimToNull(form.getBankName()))
                        .bankAccountName(trimToNull(form.getBankAccountName()))
                        .bankAccountNumber(trimToNull(form.getBankAccountNumber()))
                        .bankBranch(trimToNull(form.getBankBranch()))
                        .build())
                .slug(resolveApprovedMerchantSlug(request, form, displayName))
                .contactEmail(normalizeEmail(form.getContactEmail()))
                .supportHotline(trimToNull(form.getContactPhone()))
                .status(MerchantStatus.ACTIVE)
                .commissionRate(request.getData().getCommissionRate() != null ? request.getData().getCommissionRate() : new java.math.BigDecimal("15.00"))
                .build();
        Merchant savedMerchant = merchantRepository.saveAndFlush(merchant);
        assignMerchantOwnership(request, form, savedMerchant);

        OffsetDateTime now = OffsetDateTime.now();
        form.setMerchantId(savedMerchant.getId().toString());
        form.setStatus(ApplicationFormStatus.APPROVED);
        form.setApprovedAt(now);
        form.setApprovedBy(currentUser.subject());
        form.setRejectedBy(null);
        form.setRejectionReason(null);
        MerchantApplicationForm savedForm = merchantApplicationFormRepository.saveAndFlush(form);

        return buildReviewResponse(savedForm);
    }

    @Override
    @Transactional
    public ReviewMerchantApplicationResponse rejectMerchantApplication(RejectMerchantApplicationRequest request) {
        authorizeAdmin(request);

        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        UUID id = request.getData().getId();
        MerchantApplicationForm form = requireReviewableApplicationForm(request, id);

        form.setStatus(ApplicationFormStatus.REJECTED);
        form.setRejectedBy(currentUser.subject());
        form.setRejectionReason(request.getData().getReason().trim());
        MerchantApplicationForm savedForm = merchantApplicationFormRepository.saveAndFlush(form);

        return buildReviewResponse(savedForm);
    }

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw businessException(request, INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }

    private MerchantApplicationForm requireApplicationForm(BaseRequest request, UUID id) {
        return merchantApplicationFormRepository.findById(id)
                .orElseThrow(() -> businessException(
                        request,
                        RECORD_NOT_FOUND,
                        String.format("Merchant application form with id %s not found", id)
                ));
    }

    private MerchantApplicationForm requireReviewableApplicationForm(BaseRequest request, UUID id) {
        MerchantApplicationForm form = requireApplicationForm(request, id);
        if (!isReviewable(form.getStatus())) {
            throw businessException(
                    request,
                    INVALID_INPUT_ERROR,
                    "Merchant application form is not reviewable in status " + form.getStatus()
            );
        }
        return form;
    }

    private boolean isReviewable(ApplicationFormStatus status) {
        return status == ApplicationFormStatus.SUBMITTED
                || status == ApplicationFormStatus.UNDER_REVIEW
                || status == ApplicationFormStatus.NEEDS_REVISION;
    }

    private void validatePagination(BaseRequest request, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_NUMBER);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_SIZE);
        }
    }

    private ApplicationFormStatus parseStatus(BaseRequest request, String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ApplicationFormStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw businessException(
                    request,
                    INVALID_INPUT_ERROR,
                    "Invalid status. Allowed values: " + String.join(", ", ApplicationFormStatus.valuesAsString())
            );
        }
    }

    private GetMerchantApplicationFormsResponse.MerchantApplicationFormItem mapApplicationFormItem(MerchantApplicationForm form) {
        return GetMerchantApplicationFormsResponse.MerchantApplicationFormItem.builder()
                .id(form.getId())
                .formCode(form.getFormCode())
                .submittedBy(form.getSubmittedBy())
                .legalName(form.getLegalName())
                .displayName(form.getDisplayName())
                .slug(form.getSlug())
                .ownerFullName(form.getOwnerFullName())
                .ownerEmail(form.getOwnerEmail())
                .ownerPhone(form.getOwnerPhone())
                .contactName(form.getContactName())
                .contactEmail(form.getContactEmail())
                .contactPhone(form.getContactPhone())
                .status(form.getStatus() != null ? form.getStatus().name() : null)
                .submittedAt(form.getSubmittedAt())
                .city(form.getCity())
                .province(form.getProvince())
                .country(form.getCountry())
                .build();
    }

    private ReviewMerchantApplicationResponse buildReviewResponse(MerchantApplicationForm form) {
        return ReviewMerchantApplicationResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(ReviewMerchantApplicationResponse.ReviewMerchantApplicationResponseData.builder()
                        .id(form.getId())
                        .formCode(form.getFormCode())
                        .merchantId(form.getMerchantId())
                        .status(form.getStatus() != null ? form.getStatus().name() : null)
                        .approvedAt(form.getApprovedAt())
                        .approvedBy(form.getApprovedBy())
                        .rejectedBy(form.getRejectedBy())
                        .rejectionReason(form.getRejectionReason())
                        .updatedAt(form.getUpdatedAt())
                        .build())
                .build();
    }

    private void validateMerchantUniqueness(BaseRequest request, MerchantApplicationForm form) {
        String legalName = trimToNull(form.getLegalName());
        if (legalName != null && merchantRepository.existsByLegalName(legalName)) {
            throw businessException(request, DUPLICATE_ERROR, String.format(MERCHANT_EXISTS_MESSAGE, legalName));
        }

        String taxCode = trimToNull(form.getTaxCode());
        if (taxCode != null && merchantRepository.existsByTaxCode(taxCode)) {
            throw businessException(request, DUPLICATE_ERROR, String.format(MERCHANT_EXISTS_MESSAGE, taxCode));
        }

        String registrationNumber = trimToNull(form.getBusinessRegistrationNumber());
        if (registrationNumber != null && merchantRepository.existsByBusinessRegistrationNumber(registrationNumber)) {
            throw businessException(request, DUPLICATE_ERROR, String.format(MERCHANT_EXISTS_MESSAGE, registrationNumber));
        }

        String contactEmail = normalizeEmail(form.getContactEmail());
        if (contactEmail != null && merchantRepository.existsByContactEmailIgnoreCase(contactEmail)) {
            throw businessException(request, DUPLICATE_ERROR, MERCHANT_EXISTS_MESSAGE);
        }

        String slug = trimToNull(form.getSlug());
        if (slug != null && (merchantRepository.existsBySlug(slug) || storeRepository.existsBySlug(slug))) {
            throw businessException(request, DUPLICATE_ERROR, MERCHANT_SLUG_EXISTS_MESSAGE);
        }
    }

    private String resolveDisplayName(BaseRequest request, MerchantApplicationForm form) {
        String displayName = trimToNull(form.getDisplayName());
        if (displayName != null) {
            return displayName;
        }

        String merchantName = trimToNull(form.getMerchantName());
        if (merchantName != null) {
            return merchantName;
        }

        String legalName = trimToNull(form.getLegalName());
        if (legalName != null) {
            return legalName;
        }

        throw businessException(request, INVALID_INPUT_ERROR, "Merchant application form is missing display name");
    }

    private String normalizeEmail(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private void assignMerchantOwnership(BaseRequest request, MerchantApplicationForm form, Merchant merchant) {
        UUID submittedBy = parseSubmittedBy(request, form.getSubmittedBy());
        var user = userRepository.findById(submittedBy)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Submitted user not found"));

        userRoleService.assignRoleIfAbsent(user, MERCHANT_OWNER_ROLE);

        if (merchantMemberRepository.existsByMerchantIdAndUserId(merchant.getId(), user.getId())) {
            return;
        }

        MerchantMember merchantMember = MerchantMember.builder()
                .merchant(merchant)
                .user(user)
                .role(MerchantMemberRole.OWNER)
                .status(MerchantMemberStatus.ACTIVE)
                .assignedAt(Instant.now())
                .build();
        merchantMemberRepository.saveAndFlush(merchantMember);
    }

    private UUID parseSubmittedBy(BaseRequest request, String submittedBy) {
        try {
            return UUID.fromString(submittedBy);
        } catch (Exception exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid submittedBy in merchant application form");
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

    private String resolveApprovedMerchantSlug(BaseRequest request, MerchantApplicationForm form, String displayName) {
        String requestedSlug = trimToNull(form.getSlug());
        if (requestedSlug == null) {
            return buildMerchantSlug(displayName);
        }
        String normalizedSlug = slugify(requestedSlug);
        if (merchantRepository.existsBySlug(normalizedSlug) || storeRepository.existsBySlug(normalizedSlug)) {
            throw businessException(request, DUPLICATE_ERROR, MERCHANT_SLUG_EXISTS_MESSAGE);
        }
        return normalizedSlug;
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
