package vn.com.orchestration.foodios.jwt.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class IdentityUserContextProvider {


    public IdentityUserContext requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof IdentityUserContext userContext)) {
            throw new IllegalStateException("Current principal is not IdentityUserContext");
        }

        return userContext;
    }
}
