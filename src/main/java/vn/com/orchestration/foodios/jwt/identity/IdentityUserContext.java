package vn.com.orchestration.foodios.jwt.identity;

import java.util.Set;

public record IdentityUserContext(
        String subject,
        String username,
        String email,
        Set<String> roles
) {
}
