package vn.com.orchestration.foodios.service.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupRequest;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupResponse;
import vn.com.orchestration.foodios.entity.merchant.ApplicationFormStatus;
import vn.com.orchestration.foodios.entity.merchant.MerchantApplicationForm;
import vn.com.orchestration.foodios.entity.merchant.MerchantApplicationFormRepository;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.merchant.MerchantManagementService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_EXISTS;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;


@Service
@RequiredArgsConstructor
public class MerchantManagementServiceImpl implements MerchantManagementService {

    private final MerchantRepository merchantRepository;
    private final MerchantApplicationFormRepository merchantApplicationFormRepository;
    private final UserRepository userRepository;
    private final ApiResultFactory apiResultFactory;

    @Transactional
    @Override
    public MerchantSignupResponse signup(MerchantSignupRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        String legalName = request.getData().getMerchant().getLegalName();
        String taxCode = request.getData().getMerchant().getTaxCode();
        String registrationNo = request.getData().getMerchant().getBusinessRegistrationNumber();

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
                .displayName(request.getData().getMerchant().getDisplayName())
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
}
