package vn.com.orchestration.foodios.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetUsersByRoleResponse extends BaseResponse<GetUsersByRoleResponse.GetUsersByRoleResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetUsersByRoleResponseData {
        private List<UserPayload> items;
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
    @Builder
    public static class UserPayload {
        private String id;
        private String username;
        private String email;
        private String phone;
        private String fullName;
        private String avatarUrl;
        private String status;
        private String createdAt;
    }
}
