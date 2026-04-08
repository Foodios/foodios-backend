package vn.com.orchestration.foodios.service.merchant.impl;

import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.AddMerchantDriverRequest;
import vn.com.orchestration.foodios.dto.merchant.AddMerchantDriverResponse;
import vn.com.orchestration.foodios.dto.merchant.DeleteMerchantDriverResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantDriversResponse;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantMember;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.user.Role;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserRole;
import vn.com.orchestration.foodios.entity.user.UserRoleId;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.RoleRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.repository.UserRoleRepository;
import vn.com.orchestration.foodios.service.merchant.MerchantMemberService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class MerchantMemberServiceImpl implements MerchantMemberService {

    private final MerchantRepository merchantRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    private static final String DRIVER_ROLE_CODE = "DRIVER";
    private static final String CUSTOMER_ROLE_CODE = "CUSTOMER";

    @Override
    @Transactional
    public DeleteMerchantDriverResponse deleteDriver(UUID merchantId, UUID userId, BaseRequest request) {
        // 1. Resolve Current User and Authorize Access
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        // 2. Find Member to delete
        MerchantMember member = merchantMemberRepository.findByMerchantIdAndUserId(merchantId, userId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Driver not found in this merchant"));

        if (member.getRole() != MerchantMemberRole.DRIVER) {
            throw businessException(request, INVALID_INPUT_ERROR, "Member is not a driver");
        }

        // 3. Remove from Merchant Members
        merchantMemberRepository.delete(member);

        // 4. Revert User System Role to CUSTOMER
        revertToCustomerRole(member.getUser());

        return DeleteMerchantDriverResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(DeleteMerchantDriverResponse.DeleteMerchantDriverResponseData.builder()
                        .userId(userId.toString())
                        .status("REMOVED")
                        .build())
                .build();
    }

    private void revertToCustomerRole(User user) {
        Role customerRole = roleRepository.findByCode(CUSTOMER_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("Role CUSTOMER is not preloaded"));

        // Remove all current roles (which should include DRIVER)
        userRoleRepository.deleteByIdUserId(user.getId());
        userRoleRepository.flush();

        // Assign back CUSTOMER role
        UserRole userRole = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(user.getId())
                        .roleId(customerRole.getId())
                        .build())
                .user(user)
                .role(customerRole)
                .assignedAt(OffsetDateTime.now())
                .build();
        userRoleRepository.save(userRole);
    }

    @Override
    @Transactional
    public AddMerchantDriverResponse addDriver(AddMerchantDriverRequest request) {
        UUID merchantId = UUID.fromString(request.getData().getMerchantId());
        UUID userId = UUID.fromString(request.getData().getUserId());

        // 1. Resolve Current User and Authorize Access
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        // 2. Find Merchant
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));

        // 3. Find User to be promoted
        User user = userRepository.findById(userId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "User not found"));

        // 4. Update User System Role to DRIVER
        updateToDriverRole(user);

        // 5. Add User as Merchant Member
        MerchantMember member = addMerchantMember(merchant, user, MerchantMemberRole.DRIVER);

        return AddMerchantDriverResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(AddMerchantDriverResponse.AddMerchantDriverResponseData.builder()
                        .memberId(member.getId().toString())
                        .status(member.getStatus().name())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantDriversResponse getDrivers(UUID merchantId, BaseRequest request) {
        // 1. Resolve Current User and Authorize Access
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        // 2. Fetch Drivers for this merchant
        List<MerchantMember> driverMembers = merchantMemberRepository.findByMerchantId(merchantId).stream()
                .filter(member -> member.getRole() == MerchantMemberRole.DRIVER)
                .toList();

        // 3. Map to Payload
        List<GetMerchantDriversResponse.DriverPayload> drivers = driverMembers.stream()
                .map(member -> {
                    User driverUser = member.getUser();
                    return GetMerchantDriversResponse.DriverPayload.builder()
                            .memberId(member.getId().toString())
                            .userId(driverUser.getId().toString())
                            .fullName(driverUser.getFullName())
                            .email(driverUser.getEmail())
                            .phone(driverUser.getPhone())
                            .status(member.getStatus().name())
                            .assignedAt(member.getAssignedAt())
                            .avatarUrl(driverUser.getAvatarUrl())
                            .build();
                })
                .toList();

        return GetMerchantDriversResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(GetMerchantDriversResponse.GetMerchantDriversResponseData.builder()
                        .drivers(drivers)
                        .build())
                .build();
    }

    private void updateToDriverRole(User user) {
        Role driverRole = roleRepository.findByCode(DRIVER_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("Role DRIVER is not preloaded"));

        // Remove all current roles for this user as they "become" a driver
        userRoleRepository.deleteByIdUserId(user.getId());
        userRoleRepository.flush();

        UserRole userRole = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(user.getId())
                        .roleId(driverRole.getId())
                        .build())
                .user(user)
                .role(driverRole)
                .assignedAt(OffsetDateTime.now())
                .build();
        userRoleRepository.save(userRole);
    }

    private MerchantMember addMerchantMember(Merchant merchant, User user, MerchantMemberRole role) {
        return merchantMemberRepository.findByMerchantIdAndUserId(merchant.getId(), user.getId())
                .map(existingMember -> {
                    existingMember.setRole(role);
                    existingMember.setStatus(MerchantMemberStatus.ACTIVE);
                    existingMember.setAssignedAt(Instant.now());
                    return merchantMemberRepository.save(existingMember);
                })
                .orElseGet(() -> {
                    MerchantMember newMember = MerchantMember.builder()
                            .merchant(merchant)
                            .user(user)
                            .role(role)
                            .status(MerchantMemberStatus.ACTIVE)
                            .assignedAt(Instant.now())
                            .build();
                    return merchantMemberRepository.save(newMember);
                });
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }

    private User resolveCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isBlank()) {
            return userRepository.findByEmail(currentUser.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        try {
            UUID userId = UUID.fromString(currentUser.subject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        } catch (IllegalArgumentException exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid current user");
        }
    }

    private void authorizeMerchantAccess(BaseRequest request, UUID merchantId, UUID userId) {
        boolean hasAccess = merchantMemberRepository.existsByMerchantIdAndUserIdAndStatusAndRoleIn(
                merchantId,
                userId,
                MerchantMemberStatus.ACTIVE,
                List.of(MerchantMemberRole.OWNER, MerchantMemberRole.MANAGER)
        );
        if (!hasAccess) {
            throw businessException(request, INVALID_INPUT_ERROR, MERCHANT_ACCESS_DENIED_MESSAGE);
        }
    }
}
