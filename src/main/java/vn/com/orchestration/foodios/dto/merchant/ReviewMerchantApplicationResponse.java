package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReviewMerchantApplicationResponse
        extends BaseResponse<ReviewMerchantApplicationResponse.ReviewMerchantApplicationResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class ReviewMerchantApplicationResponseData {
        private UUID id;
        private String formCode;
        private String merchantId;
        private String status;
        private OffsetDateTime approvedAt;
        private String approvedBy;
        private String rejectedBy;
        private String rejectionReason;
        private OffsetDateTime updatedAt;
    }
}
