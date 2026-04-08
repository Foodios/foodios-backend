package vn.com.orchestration.foodios.service.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetMyProfileResponse;
import vn.com.orchestration.foodios.dto.user.UpdateProfileRequest;
import vn.com.orchestration.foodios.dto.user.UpdateProfileResponse;
import vn.com.orchestration.foodios.entity.loyalty.CustomerMembership;
import vn.com.orchestration.foodios.entity.loyalty.MembershipTier;
import vn.com.orchestration.foodios.entity.user.Authority;
import vn.com.orchestration.foodios.entity.user.Role;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserRole;
import vn.com.orchestration.foodios.entity.user.UserStatus;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.repository.AuthorityRepository;
import vn.com.orchestration.foodios.repository.CustomerMembershipRepository;
import vn.com.orchestration.foodios.repository.RoleRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.repository.UserRoleRepository;
import vn.com.orchestration.foodios.service.user.UserInformationService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.EMAIL_EXISTS;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_USER_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MEMBERSHIP_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.OPERATION_NOT_ALLOWED;
import static vn.com.orchestration.foodios.constant.ErrorConstant.PHONE_NUMBER_EXISTS;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_ACTIVE_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class UserInformationServiceImpl implements UserInformationService {

    private final UserRepository userRepository;
    private final ApiResultFactory apiResultFactory;
    private final CustomerMembershipRepository customerMembershipRepository;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRoleRepository userRoleRepository;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    @Transactional(readOnly = true)
    public GetMyProfileResponse getMyProfile(BaseRequest request, String userId) {
        if(userId == null) {
            throw new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, INVALID_USER_MESSAGE));
        }
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE)));

        CustomerMembership customerMembership = customerMembershipRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, MEMBERSHIP_NOT_FOUND_MESSAGE)));

        MembershipTier membershipTier = customerMembership.getMembershipTier();
        List<UserRole> userRole = userRoleRepository.findByIdUserId(UUID.fromString(userId));

        Set<String> roles = userRole
                .stream()
                .map(r -> r.getRole().getCode())
                .collect(Collectors.toSet());

        Set<String> authorities = userRole
                .stream()
                .flatMap(r -> r.getRole().getAuthorities().stream())
                .map(Authority::getCode)
                .collect(Collectors.toSet());

        return GetMyProfileResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .authorities(GetMyProfileResponse.GetMyProfileAuthorities.builder()
                        .roles(roles)
                        .authorities(authorities)
                        .build())
                .membership(GetMyProfileResponse.GetMyProfileMembership.builder()
                        .badge(membershipTier.getBadge())
                        .discountPercent(membershipTier.getDiscountPercent())
                        .status(customerMembership.getStatus())
                        .joinedAt(customerMembership.getJoinedAt())
                        .promotedAt(customerMembership.getPromotedAt())
                        .pointToNextTier(customerMembership.getPointsToNextTier())
                        .pointMultiplier(customerMembership.getPointMultiplier())
                        .currentAvailablePoints(customerMembership.getCurrentAvailablePoints())
                        .totalPoints(customerMembership.getTotalPoints())
                        .build()
                )
                .data(GetMyProfileResponse.GetMyProfileResponseData.builder()
                        .userId(userId)
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .fullName(user.getFullName())
                        .profileCompleted(user.isProfileCompleted())
                        .status(user.getStatus())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public UpdateProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getData().getUserId()))
                .orElseThrow(() -> new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE)));

        if(!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                    ExceptionUtils.buildResultResponse(OPERATION_NOT_ALLOWED, USER_NOT_ACTIVE_MESSAGE));
        }

        if (request.getData().getEmail() != null && !request.getData().getEmail().equals(user.getEmail())) {
            boolean emailExists = userRepository.existsByEmail(request.getData().getEmail());
            if (emailExists) {
                throw new BusinessException(
                        request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, EMAIL_EXISTS)
                );
            }
        }

        if(request.getData().getPhone() != null && !request.getData().getPhone().equals(user.getPhone())) {
            boolean phoneNumberExists = userRepository.existsByPhone(request.getData().getPhone());
            if(phoneNumberExists) {
                throw new BusinessException(
                        request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(DUPLICATE_ERROR, PHONE_NUMBER_EXISTS)
                );
            }
        }

        Optional.ofNullable(request.getData().getFullName())
                .ifPresent(user::setFullName);

        Optional.ofNullable(request.getData().getPhone())
                .ifPresent(user::setPhone);

        Optional.ofNullable(request.getData().getEmail())
                .ifPresent(user::setEmail);

        Optional.ofNullable(request.getData().getAvatarUrl())
                .ifPresent(user::setAvatarUrl);

        userRepository.save(user);
        sLog.info("[USER-PROFILE] Saved updated user information successfully");


        return UpdateProfileResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(UpdateProfileResponse.UpdateProfileResponseData
                        .builder()
                        .userId(request.getData().getUserId())
                        .phone(request.getData().getPhone())
                        .email(request.getData().getEmail())
                        .avatarUrl(request.getData().getAvatarUrl())
                        .fullName(request.getData().getFullName())
                        .build())
                .build();
    }
}
