package vn.com.orchestration.foodios.jwt.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.jwt.JwtProperties;
import vn.com.orchestration.foodios.jwt.JwtService;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    @Override
    public String generateAccessToken(User user, Set<String> roles, Set<String> authorities) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, "access");
        claims.put(CLAIM_EMAIL, user.getEmail());
        claims.put(CLAIM_ROLES, roles);
        claims.put(CLAIM_AUTHORITIES, authorities);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, "refresh")
                .claim(CLAIM_EMAIL, user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public String extractTokenType(String token) {
        return extractTokenType(extractAllClaims(token));
    }

    @Override
    public String extractTokenType(Claims claims) {
        String legacy = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!isBlank(legacy)) {
            return legacy;
        }
        return claims.get(CLAIM_TYPE, String.class);
    }

    @Override
    public OffsetDateTime extractIssuedAt(String token) {
        Date issuedAt = extractAllClaims(token).getIssuedAt();
        return issuedAt == null ? null : OffsetDateTime.ofInstant(issuedAt.toInstant(), ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime extractExpiration(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration == null ? null : OffsetDateTime.ofInstant(expiration.toInstant(), ZoneOffset.UTC);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

