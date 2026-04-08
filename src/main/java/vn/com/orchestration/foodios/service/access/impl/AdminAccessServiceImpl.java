package vn.com.orchestration.foodios.service.access.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.access.CreateAuthorityRequest;
import vn.com.orchestration.foodios.dto.access.CreateAuthorityResponse;
import vn.com.orchestration.foodios.dto.access.CreateRoleRequest;
import vn.com.orchestration.foodios.dto.access.CreateRoleResponse;
import vn.com.orchestration.foodios.dto.access.SetRoleAuthoritiesRequest;
import vn.com.orchestration.foodios.dto.access.SetRoleAuthoritiesResponse;
import vn.com.orchestration.foodios.dto.access.SetUserRoleRequest;
import vn.com.orchestration.foodios.dto.access.SetUserRoleResponse;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.user.Authority;
import vn.com.orchestration.foodios.entity.user.Role;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserRole;
import vn.com.orchestration.foodios.entity.user.UserRoleId;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.AuthorityRepository;
import vn.com.orchestration.foodios.repository.RoleRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.repository.UserRoleRepository;
import vn.com.orchestration.foodios.service.access.AdminAccessService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static vn.com.orchestration.foodios.constant.ErrorConstant.ADMIN_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.AUTHORITY_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.AUTHORITY_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.ROLE_AUTHORITIES_UPDATED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.ROLE_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.ROLE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_ROLE_UPDATED_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAccessServiceImpl implements AdminAccessService {

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    @Override
    @Transactional
    public CreateRoleResponse createRole(CreateRoleRequest request) {
        authorizeAdmin(request);

        CreateRoleRequest.CreateRoleRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        String code = normalizeCode(data.getCode());
        if (roleRepository.existsByCode(code)) {
            throw businessException(request, DUPLICATE_ERROR, ROLE_EXISTS_MESSAGE);
        }

        Role role = Role.builder()
                .code(code)
                .name(data.getName().trim())
                .description(trimToNull(data.getDescription()))
                .enabled(data.getEnabled() == null || data.getEnabled())
                .build();
        Role savedRole = roleRepository.saveAndFlush(role);

        CreateRoleResponse response = new CreateRoleResponse();
        response.setResult(successResult(SUCCESS_MESSAGE));
        response.setData(
                CreateRoleResponse.CreateRoleResponseData.builder()
                        .id(savedRole.getId())
                        .code(savedRole.getCode())
                        .name(savedRole.getName())
                        .description(savedRole.getDescription())
                        .enabled(savedRole.isEnabled())
                        .build()
        );
        return response;
    }

    @Override
    @Transactional
    public CreateAuthorityResponse createAuthority(CreateAuthorityRequest request) {
        authorizeAdmin(request);

        CreateAuthorityRequest.CreateAuthorityRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        String code = normalizeCode(data.getCode());
        if (authorityRepository.existsByCode(code)) {
            throw businessException(request, DUPLICATE_ERROR, AUTHORITY_EXISTS_MESSAGE);
        }

        Authority authority = Authority.builder()
                .code(code)
                .name(data.getName().trim())
                .description(trimToNull(data.getDescription()))
                .enabled(data.getEnabled() == null || data.getEnabled())
                .build();
        Authority savedAuthority = authorityRepository.saveAndFlush(authority);

        CreateAuthorityResponse response = new CreateAuthorityResponse();
        response.setResult(successResult(SUCCESS_MESSAGE));
        response.setData(
                CreateAuthorityResponse.CreateAuthorityResponseData.builder()
                        .id(savedAuthority.getId())
                        .code(savedAuthority.getCode())
                        .name(savedAuthority.getName())
                        .description(savedAuthority.getDescription())
                        .enabled(savedAuthority.isEnabled())
                        .build()
        );
        return response;
    }

    @Override
    @Transactional
    public SetUserRoleResponse setUserRole(SetUserRoleRequest request) {
        authorizeAdmin(request);

        SetUserRoleRequest.SetUserRoleRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        User user = userRepository.findById(data.getUserId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        Role role = roleRepository.findByCode(normalizeCode(data.getRoleCode()))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, ROLE_NOT_FOUND_MESSAGE));

        userRoleRepository.deleteByIdUserId(user.getId());

        UserRole userRole = UserRole.builder()
                .id(UserRoleId.builder().userId(user.getId()).roleId(role.getId()).build())
                .user(user)
                .role(role)
                .assignedAt(OffsetDateTime.now())
                .build();
        userRoleRepository.saveAndFlush(userRole);

        SetUserRoleResponse response = new SetUserRoleResponse();
        response.setResult(successResult(USER_ROLE_UPDATED_MESSAGE));
        response.setData(
                SetUserRoleResponse.SetUserRoleResponseData.builder()
                        .userId(user.getId())
                        .roleCode(role.getCode())
                        .updated(true)
                        .build()
        );
        return response;
    }

    @Override
    @Transactional
    public SetRoleAuthoritiesResponse setRoleAuthorities(SetRoleAuthoritiesRequest request) {
        authorizeAdmin(request);

        SetRoleAuthoritiesRequest.SetRoleAuthoritiesRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        Role role = roleRepository.findByCode(normalizeCode(data.getRoleCode()))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, ROLE_NOT_FOUND_MESSAGE));

        List<String> normalizedCodes = new ArrayList<>();
        for (String authorityCode : data.getAuthorityCodes()) {
            if (authorityCode == null || authorityCode.isBlank()) {
                continue;
            }
            normalizedCodes.add(normalizeCode(authorityCode));
        }
        if (normalizedCodes.isEmpty()) {
            throw businessException(request, INVALID_INPUT_ERROR, "authorityCodes must not be empty");
        }

        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>(normalizedCodes);
        for (String authorityCode : uniqueCodes) {
            Authority authority = authorityRepository.findByCode(authorityCode)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, AUTHORITY_NOT_FOUND_MESSAGE));
            role.getAuthorities().add(authority);
        }

        Role savedRole = roleRepository.saveAndFlush(role);
        List<String> assignedCodes = savedRole.getAuthorities().stream()
                .map(Authority::getCode)
                .sorted()
                .toList();

        SetRoleAuthoritiesResponse response = new SetRoleAuthoritiesResponse();
        response.setResult(successResult(ROLE_AUTHORITIES_UPDATED_MESSAGE));
        response.setData(
                SetRoleAuthoritiesResponse.SetRoleAuthoritiesResponseData.builder()
                        .roleCode(savedRole.getCode())
                        .authorityCodes(assignedCodes)
                        .updated(true)
                        .build()
        );
        return response;
    }

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw businessException(request, INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ApiResult successResult(String description) {
        return ApiResult.builder().responseCode(SUCCESS_CODE).description(description).build();
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
