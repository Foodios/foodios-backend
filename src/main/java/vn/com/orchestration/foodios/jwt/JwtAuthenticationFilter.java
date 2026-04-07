package vn.com.orchestration.foodios.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticateFromTokenIfPossible(request, token);
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            return null;
        }
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private void authenticateFromTokenIfPossible(HttpServletRequest request, String token) {
        Claims claims = jwtService.extractAllClaims(token);

        String userId = claims.getSubject();
        if (userId == null || userId.isBlank()) {
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String tokenType = jwtService.extractTokenType(claims);
        if (isRefreshToken(tokenType)) {
            return;
        }

        String principal = resolvePrincipal(claims, userId);
        Set<SimpleGrantedAuthority> grantedAuthorities = extractAuthorities(claims);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, grantedAuthorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isRefreshToken(String tokenType) {
        if (tokenType == null) {
            return false;
        }
        return "refresh".equals(tokenType.trim().toLowerCase(Locale.ROOT));
    }

    private String resolvePrincipal(Claims claims, String fallbackUserId) {
        String email = claims.get(JwtService.CLAIM_EMAIL, String.class);
        if (email != null && !email.isBlank()) {
            return email;
        }
        return fallbackUserId;
    }

    private Set<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

        Object rolesClaim = claims.get("roles");
        addAuthoritiesFromClaim(grantedAuthorities, rolesClaim, true);

        Object authoritiesClaim = claims.get("authorities");
        addAuthoritiesFromClaim(grantedAuthorities, authoritiesClaim, false);

        return grantedAuthorities;
    }

    private void addAuthoritiesFromClaim(
            Set<SimpleGrantedAuthority> grantedAuthorities,
            Object claim,
            boolean normalizeRolePrefix
    ) {
        if (!(claim instanceof Collection<?> values)) {
            return;
        }

        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String raw = value.toString();
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            String authority = trimmed;
            if (normalizeRolePrefix) {
                authority = authority.startsWith("ROLE_") ? authority : "ROLE_" + authority;
            }
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
    }
}
