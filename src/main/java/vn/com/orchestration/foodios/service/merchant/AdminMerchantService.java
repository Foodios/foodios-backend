package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.ApproveMerchantApplicationRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantApplicationFormDetailResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantApplicationFormsResponse;
import vn.com.orchestration.foodios.dto.merchant.RejectMerchantApplicationRequest;
import vn.com.orchestration.foodios.dto.merchant.ReviewMerchantApplicationResponse;

import java.util.UUID;

public interface AdminMerchantService {
    CreateMerchantResponse createMerchant(CreateMerchantRequest request);

    GetMerchantApplicationFormsResponse getMerchantApplicationForms(
            BaseRequest request,
            String status,
            Integer pageNumber,
            Integer pageSize
    );

    GetMerchantApplicationFormDetailResponse getMerchantApplicationFormDetail(BaseRequest request, UUID id);

    ReviewMerchantApplicationResponse approveMerchantApplication(ApproveMerchantApplicationRequest request, UUID id);

    ReviewMerchantApplicationResponse rejectMerchantApplication(RejectMerchantApplicationRequest request, UUID id);
}
