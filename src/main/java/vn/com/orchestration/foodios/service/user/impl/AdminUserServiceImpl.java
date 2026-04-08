package vn.com.orchestration.foodios.service.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetUsersByRoleResponse;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.user.AdminUserService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;
import vn.com.orchestration.foodios.exception.BusinessException;

import java.util.List;
import java.util.Set;

import static vn.com.orchestration.foodios.constant.ErrorConstant.ADMIN_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_NUMBER;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_SIZE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");

    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    @Override
    @Transactional(readOnly = true)
    public GetUsersByRoleResponse getUsersByRoles(BaseRequest request, List<String> roleCodes, Integer pageNumber, Integer pageSize) {
        authorizeAdmin(request);
        validatePagination(request, pageNumber, pageSize);

        PageRequest pageable = PageRequest.of(
                pageNumber - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> users = userRepository.findByRoleCodes(roleCodes, pageable);

        return GetUsersByRoleResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetUsersByRoleResponse.GetUsersByRoleResponseData.builder()
                        .items(users.getContent().stream().map(this::mapUserItem).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(users.getTotalElements())
                        .totalPages(users.getTotalPages())
                        .hasNext(users.hasNext())
                        .build())
                .build();
    }

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw businessException(request, INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }

    private void validatePagination(BaseRequest request, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_NUMBER);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_SIZE);
        }
    }

    private GetUsersByRoleResponse.UserPayload mapUserItem(User user) {
        return GetUsersByRoleResponse.UserPayload.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
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
