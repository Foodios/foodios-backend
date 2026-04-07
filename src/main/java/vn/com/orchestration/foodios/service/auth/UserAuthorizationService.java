package vn.com.orchestration.foodios.service.auth;

import vn.com.orchestration.foodios.entity.user.User;

import java.util.Set;

public interface UserAuthorizationService {
    Set<String> getRoles(User user);

    Set<String> getAuthorities(User user);
}

