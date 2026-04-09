package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.ApproveMerchantApplicationRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantApplicationFormDetailResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantApplicationFormsResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantDetailResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantsResponse;
import vn.com.orchestration.foodios.dto.merchant.RejectMerchantApplicationRequest;
import vn.com.orchestration.foodios.dto.merchant.ReviewMerchantApplicationResponse;
import vn.com.orchestration.foodios.dto.merchant.UpdateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.UpdateMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.DeleteMerchantResponse;

import java.util.UUID;

public interface AdminMerchantService {
    CreateMerchantResponse createMerchant(CreateMerchantRequest request);

    UpdateMerchantResponse updateMerchant(UpdateMerchantRequest request);

    DeleteMerchantResponse deleteMerchant(BaseRequest request, UUID id);

    GetMerchantsResponse getMerchants(BaseRequest request, String query, Integer pageNumber, Integer pageSize);

    GetMerchantDetailResponse getMerchantDetail(BaseRequest request, UUID id);

    GetMerchantApplicationFormsResponse getMerchantApplicationForms(
            BaseRequest request,
            String status,
            Integer pageNumber,
            Integer pageSize
    );

    GetMerchantApplicationFormDetailResponse getMerchantApplicationFormDetail(BaseRequest request, UUID id);

    ReviewMerchantApplicationResponse approveMerchantApplication(ApproveMerchantApplicationRequest request);

    ReviewMerchantApplicationResponse rejectMerchantApplication(RejectMerchantApplicationRequest request);
}
