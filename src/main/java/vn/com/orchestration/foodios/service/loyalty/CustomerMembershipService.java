package vn.com.orchestration.foodios.service.loyalty;

import vn.com.orchestration.foodios.entity.loyalty.CustomerMembership;
import vn.com.orchestration.foodios.entity.user.User;

public interface CustomerMembershipService {
    CustomerMembership createForNewCustomer(User user);
}

