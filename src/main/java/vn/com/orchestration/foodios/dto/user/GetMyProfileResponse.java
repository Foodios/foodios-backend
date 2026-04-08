package vn.com.orchestration.foodios.dto.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.user.UserStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMyProfileResponse extends BaseResponse<GetMyProfileResponse.GetMyProfileResponseData> {
    private GetMyProfileMembership membership;
    private GetMyProfileAuthorities authorities;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class GetMyProfileAuthorities {
        private Set<String> roles;
        private Set<String> authorities;

    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class GetMyProfileMembership {
        private String badge;
        private BigDecimal discountPercent;
        private String status;
        private OffsetDateTime joinedAt;
        private OffsetDateTime promotedAt;
        private BigDecimal pointToNextTier;
        private BigDecimal pointMultiplier;
        private BigDecimal currentAvailablePoints;
        private BigDecimal totalPoints;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class GetMyProfileResponseData {
        private String userId;
        private String username;
        private String email;
        private String phone;
        private String fullName;
        private boolean profileCompleted;
        private UserStatus status;
        private String avatarUrl;
    }

}
