package vn.com.orchestration.foodios.service.auth;

import vn.com.orchestration.foodios.entity.user.User;

public interface UserRoleService {
    void assignDefaultCustomerRole(User user);
}

