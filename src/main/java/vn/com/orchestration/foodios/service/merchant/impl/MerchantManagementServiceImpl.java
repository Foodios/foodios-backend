package vn.com.orchestration.foodios.service.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.GetMyMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.SearchMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.SearchMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupRequest;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupResponse;
import vn.com.orchestration.foodios.entity.merchant.ApplicationFormStatus;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantApplicationForm;
import vn.com.orchestration.foodios.entity.merchant.MerchantApplicationFormRepository;
import vn.com.orchestration.foodios.entity.merchant.MerchantMember;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.repository.search.StoreSearchRepository;
import vn.com.orchestration.foodios.service.merchant.MerchantManagementService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_EXISTS;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_SLUG_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;


@Service
@RequiredArgsConstructor
public class MerchantManagementServiceImpl implements MerchantManagementService {

    private final MerchantRepository merchantRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final MerchantApplicationFormRepository merchantApplicationFormRepository;
    private final UserRepository userRepository;
    private final StoreSearchRepository storeSearchRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Transactional
    @Override
    public MerchantSignupResponse signup(MerchantSignupRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        String legalName = request.getData().getMerchant().getLegalName();
        String displayName = request.getData().getMerchant().getDisplayName();
        String taxCode = request.getData().getMerchant().getTaxCode();
        String registrationNo = request.getData().getMerchant().getBusinessRegistrationNumber();
        String merchantSlug = resolveApplicationSlug(request, request.getData().getMerchant().getSlug(), displayName, legalName);

        String formCode = merchantApplicationFormRepository.generateFormCode();

        User user = userRepository.findById(UUID.fromString(request.getData().getUserId()))
                .orElseThrow(() -> new BusinessException(
                        request.getRequestId(),
                        request.getRequestDateTime(),
                        request.getChannel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE)
                ));

        if (merchantRepository.existsByLegalName(legalName)) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, String.format(MERCHANT_EXISTS, legalName))
            );
        }

        if (taxCode != null && merchantRepository.existsByTaxCode(taxCode)) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, String.format(MERCHANT_EXISTS, taxCode))
            );
        }

        if (registrationNo != null && merchantRepository.existsByBusinessRegistrationNumber(registrationNo)) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, String.format(MERCHANT_EXISTS, registrationNo))
            );
        }

        boolean applicationExists = merchantApplicationFormRepository.existsByLegalNameAndStatusIn(
                legalName,
                List.of(ApplicationFormStatus.SUBMITTED, ApplicationFormStatus.UNDER_REVIEW)
        );

        if (applicationExists) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, String.format(MERCHANT_EXISTS, legalName))
            );
        }

        boolean slugApplicationExists = merchantApplicationFormRepository.existsBySlugIgnoreCaseAndStatusIn(
                merchantSlug,
                List.of(ApplicationFormStatus.SUBMITTED, ApplicationFormStatus.UNDER_REVIEW)
        );
        if (slugApplicationExists || merchantRepository.existsBySlug(merchantSlug)) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, MERCHANT_SLUG_EXISTS_MESSAGE)
            );
        }

        var payout = request.getData().getPayout();

        MerchantApplicationForm form = MerchantApplicationForm.builder()
                .formCode(formCode)
                .submittedBy(user.getId().toString())
                .status(ApplicationFormStatus.SUBMITTED)
                .submittedAt(now)
                .ownerFullName(request.getData().getOwner().getFullName())
                .ownerEmail(request.getData().getOwner().getEmail())
                .ownerPhone(request.getData().getOwner().getPhone())
                .legalName(legalName)
                .displayName(displayName)
                .slug(merchantSlug)
                .description(request.getData().getMerchant().getDescription())
                .taxCode(taxCode)
                .businessRegistrationNumber(registrationNo)
                .businessLicenseImageUrl(request.getData().getMerchant().getBusinessLicenseImageUrl())
                .foodSafetyLicenseImageUrl(request.getData().getMerchant().getFoodSafetyLicenseImageUrl())
                .contactPhone(request.getData().getMerchant().getContactPhone())
                .contactEmail(request.getData().getMerchant().getContactEmail())
                .contactName(request.getData().getMerchant().getContactName())
                .line1(request.getData().getAddress().getLine1())
                .line2(request.getData().getAddress().getLine2())
                .district(request.getData().getAddress().getDistrict())
                .city(request.getData().getAddress().getCity())
                .province(request.getData().getAddress().getProvince())
                .postalCode(request.getData().getAddress().getPostalCode())
                .country(request.getData().getAddress().getCountry())
                .bankName(payout != null ? payout.getBankName() : null)
                .bankAccountName(payout != null ? payout.getBankAccountName() : null)
                .bankAccountNumber(payout != null ? payout.getBankAccountNumber() : null)
                .bankBranch(payout != null ? payout.getBankBranch() : null)
                .build();

        merchantApplicationFormRepository.save(form);

        return MerchantSignupResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(MerchantSignupResponse.MerchantSignupResponseData.builder()
                        .registrationNumber(formCode)
                        .appliedAt(now)
                        .status(form.getStatus().name())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMyMerchantResponse getMyMerchant(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        UUID userId = resolveCurrentUserId(request, currentUser);

        MerchantMember merchantMember = merchantMemberRepository.findByUserIdAndStatus(userId, MerchantMemberStatus.ACTIVE)
                .stream().min((left, right) -> {
                    if (left.getAssignedAt() == null && right.getAssignedAt() == null) return 0;
                    if (left.getAssignedAt() == null) return 1;
                    if (right.getAssignedAt() == null) return -1;
                    return left.getAssignedAt().compareTo(right.getAssignedAt());
                })
                .orElseThrow(() -> new BusinessException(
                        request.getRequestId(),
                        request.getRequestDateTime(),
                        request.getChannel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, "Merchant not found for current user")
                ));

        var merchant = merchantMember.getMerchant();
        return GetMyMerchantResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(GetMyMerchantResponse.GetMyMerchantResponseData.builder()
                        .merchantId(merchant.getId())
                        .merchantName(merchant.getDisplayName())
                        .legalName(merchant.getLegalName())
                        .merchantSlug(merchant.getSlug())
                        .description(merchant.getDescription())
                        .taxCode(merchant.getTaxCode())
                        .businessRegistrationNumber(merchant.getBusinessRegistrationNumber())
                        .businessLicenseImageUrl(merchant.getBusinessLicenseImageUrl())
                        .foodSafetyLicenseImageUrl(merchant.getFoodSafetyLicenseImageUrl())
                        .logoUrl(merchant.getLogoUrl())
                        .cuisineCategory(merchant.getCuisineCategory())
                        .contactEmail(merchant.getContactEmail())
                        .supportHotline(merchant.getSupportHotline())
                        .merchantStatus(merchant.getStatus())
                        .memberRole(merchantMember.getRole() != null ? merchantMember.getRole().name() : null)
                        .memberStatus(merchantMember.getStatus() != null ? merchantMember.getStatus().name() : null)
                        .assignedAt(merchantMember.getAssignedAt())
                        .payout(GetMyMerchantResponse.MerchantPayoutInfo.builder()
                                .bankName(merchant.getMerchantPayout() != null ? merchant.getMerchantPayout().getBankName() : null)
                                .bankAccountName(merchant.getMerchantPayout() != null ? merchant.getMerchantPayout().getBankAccountName() : null)
                                .bankAccountNumber(merchant.getMerchantPayout() != null ? merchant.getMerchantPayout().getBankAccountNumber() : null)
                                .bankBranch(merchant.getMerchantPayout() != null ? merchant.getMerchantPayout().getBankBranch() : null)
                                .build())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchMerchantResponse search(SearchMerchantRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        String query = request.getName() != null ? request.getName() : "";
        
        var searchResult = storeSearchRepository.findByNameContainingIgnoreCase(query, pageable);

        List<SearchMerchantResponse.MerchantPayload> items = searchResult.getContent().stream()
                .map(doc -> SearchMerchantResponse.MerchantPayload.builder()
                        .id(doc.getId())
                        .displayName(doc.getName())
                        .slug(doc.getSlug())
                        .logoUrl(doc.getLogoUrl())
                        .description(doc.getDescription())
                        .cuisineCategory(doc.getCuisineCategory())
                        .status(doc.getStatus())
                        .build())
                .toList();

        return SearchMerchantResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(SearchMerchantResponse.SearchMerchantResponseData.builder()
                        .items(items)
                        .pageNumber(searchResult.getNumber())
                        .pageSize(searchResult.getSize())
                        .totalItems(searchResult.getTotalElements())
                        .totalPages(searchResult.getTotalPages())
                        .hasNext(searchResult.hasNext())
                        .build())
                .build();
    }

    private UUID resolveCurrentUserId(BaseRequest request, IdentityUserContext currentUser) {
        try {
            return UUID.fromString(currentUser.subject());
        } catch (Exception exception) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, "Invalid current user")
            );
        }
    }

    private String resolveApplicationSlug(MerchantSignupRequest request, String requestedSlug, String displayName, String legalName) {
        String normalizedSlug = trimToNull(requestedSlug);
        if (normalizedSlug != null) {
            return slugify(normalizedSlug);
        }

        String source = trimToNull(displayName);
        if (source == null) {
            source = trimToNull(legalName);
        }
        if (source == null) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, "Merchant display name or legal name is required")
            );
        }
        return slugify(source);
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
            return "merchant";
        }
        String slug = normalized.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "merchant" : slug;
    }
}
