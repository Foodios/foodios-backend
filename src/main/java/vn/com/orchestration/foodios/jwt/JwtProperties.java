package vn.com.orchestration.foodios.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "identity.jwt")
public class JwtProperties {
    private String secret;
    private String issuer;
    private String audience = "identity-security";
    private String authorityClaimName = "roles";
    private String authorityPrefix = "ROLE_";

    private long accessTokenExpirationMinutes = 15;
    private long refreshTokenExpirationDays = 7;
}
