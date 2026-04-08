package vn.com.orchestration.foodios.service.auth.impl;

import org.springframework.stereotype.Service;
import vn.com.orchestration.foodios.entity.user.Authority;
import vn.com.orchestration.foodios.entity.user.Role;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserRole;
import vn.com.orchestration.foodios.repository.UserRoleRepository;
import vn.com.orchestration.foodios.service.auth.UserAuthorizationService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserAuthorizationServiceImpl implements UserAuthorizationService {

    private final UserRoleRepository userRoleRepository;

    public UserAuthorizationServiceImpl(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public Set<String> getRoles(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptySet();
        }

        Set<String> roles = new HashSet<>();
        List<UserRole> userRoles = userRoleRepository.findByIdUserId(user.getId());
        for (UserRole userRole : userRoles) {
            if (userRole == null) {
                continue;
            }
            Role role = userRole.getRole();
            if (role == null || role.getCode() == null) {
                continue;
            }
            String code = role.getCode().trim();
            if (code.isBlank()) {
                continue;
            }
            roles.add(code.startsWith("ROLE_") ? code : "ROLE_" + code);
        }
        return roles;
    }

    @Override
    public Set<String> getAuthorities(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptySet();
        }

        Set<String> authorities = new HashSet<>();
        List<UserRole> userRoles = userRoleRepository.findByIdUserId(user.getId());
        for (UserRole userRole : userRoles) {
            if (userRole == null) {
                continue;
            }
            Role role = userRole.getRole();
            if (role == null || role.getAuthorities() == null) {
                continue;
            }
            for (Authority authority : role.getAuthorities()) {
                if (authority == null || authority.getCode() == null) {
                    continue;
                }
                String code = authority.getCode().trim();
                if (code.isBlank()) {
                    continue;
                }
                authorities.add(code);
            }
        }
        return authorities;
    }
}
