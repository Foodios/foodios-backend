package vn.com.orchestration.foodios.jwt;

import io.jsonwebtoken.Claims;
import vn.com.orchestration.foodios.entity.user.User;

import java.time.OffsetDateTime;
import java.util.Set;

public interface JwtService {

    String CLAIM_TYPE = "type"; // new spec
    String CLAIM_TOKEN_TYPE = "tokenType"; // legacy spec
    String CLAIM_EMAIL = "email";
    String CLAIM_USERNAME = "username";
    String CLAIM_ROLES = "roles";
    String CLAIM_AUTHORITIES = "authorities";

    String generateAccessToken(User user, Set<String> roles, Set<String> authorities);

    String generateRefreshToken(User user);

    Claims extractAllClaims(String token);

    String extractUserId(String token);

    String extractTokenType(String token);

    String extractTokenType(Claims claims);

    OffsetDateTime extractIssuedAt(String token);

    OffsetDateTime extractExpiration(String token);

    boolean isTokenValid(String token);

    boolean isRefreshToken(String token);
}
