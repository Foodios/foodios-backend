package vn.com.orchestration.foodios.service.auth.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.entity.user.Role;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserRole;
import vn.com.orchestration.foodios.entity.user.UserRoleId;
import vn.com.orchestration.foodios.repository.RoleRepository;
import vn.com.orchestration.foodios.repository.UserRoleRepository;
import vn.com.orchestration.foodios.service.auth.UserRoleService;

import java.time.OffsetDateTime;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private static final String DEFAULT_CUSTOMER_ROLE = "CUSTOMER";

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserRoleServiceImpl(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void assignDefaultCustomerRole(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must be persisted before assigning roles");
        }

        Role role = roleRepository
                .findByCode(DEFAULT_CUSTOMER_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role CUSTOMER is not preloaded"));

        UserRoleId id = UserRoleId.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .build();

        if (userRoleRepository.existsById(id)) {
            return;
        }

        UserRole userRole = UserRole.builder()
                .id(id)
                .user(user)
                .role(role)
                .assignedAt(OffsetDateTime.now())
                .build();
        userRoleRepository.saveAndFlush(userRole);
    }
}
