package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMerchantApplicationFormsResponse
        extends BaseResponse<GetMerchantApplicationFormsResponse.GetMerchantApplicationFormsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class GetMerchantApplicationFormsResponseData {
        private List<MerchantApplicationFormItem> items;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalItems;
        private Integer totalPages;
        private Boolean hasNext;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class MerchantApplicationFormItem {
        private UUID id;
        private String formCode;
        private String submittedBy;
        private String legalName;
        private String displayName;
        private String slug;
        private String ownerFullName;
        private String ownerEmail;
        private String ownerPhone;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String status;
        private OffsetDateTime submittedAt;
        private String city;
        private String province;
        private String country;
    }
}
