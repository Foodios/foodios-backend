package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMerchantDriversResponse extends BaseResponse<GetMerchantDriversResponse.GetMerchantDriversResponseData> {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class GetMerchantDriversResponseData {
        private List<DriverPayload> drivers;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DriverPayload {
        private String memberId;
        private String userId;
        private String fullName;
        private String email;
        private String phone;
        private String status;
        private Instant assignedAt;
        private String avatarUrl;
    }
}
