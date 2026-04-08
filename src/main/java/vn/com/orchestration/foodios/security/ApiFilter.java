package vn.com.orchestration.foodios.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import vn.com.orchestration.foodios.config.RequestAttributes;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.security.envelope.RequestEnvelopeExtractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiFilter extends OncePerRequestFilter {

    private final SystemLog sLog = SystemLog.getLogger(this.getClass());
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        if (shouldBypass(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean multipartRequest = isMultipartRequest(request);
        HttpServletRequest requestToUse = request;
        if (!multipartRequest) {
            requestToUse = new CachedHttpServletRequestWrapper(request);
        }
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        try {
            BaseRequest baseRequest = extractBaseRequest(requestToUse, multipartRequest);
            request.setAttribute(RequestAttributes.REQUEST_ID, baseRequest.getRequestId());
            request.setAttribute(RequestAttributes.REQUEST_DATE_TIME, baseRequest.getRequestDateTime());
            request.setAttribute(RequestAttributes.CHANNEL, baseRequest.getChannel());
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            log.warn("Invalid request envelope: {}", exception.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid Request");
            response.getWriter().flush();
            return;
        }

        try {
            filterChain.doFilter(requestToUse, cachedResponse);
        } finally {
            String responseMessage = new String(cachedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            sLog.info("{}", responseMessage);
            cachedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldBypass(String requestUri) {
        return requestUri.startsWith("/actuator/")
                || requestUri.contains("/location-service/")
                || requestUri.startsWith("/swagger-ui")
                || requestUri.startsWith("/v3/api-docs");
    }

    private BaseRequest extractBaseRequest(HttpServletRequest request, boolean multipartRequest)
            throws IOException {
        if (multipartRequest || isNonJsonBodyRequest(request)) {
            return extractFromHeadersOrParameters(request);
        }

        String requestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return RequestEnvelopeExtractor.extract(request, requestBody, objectMapper);
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private boolean isNonJsonBodyRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String contentType = request.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return true;
        }

        String normalizedContentType = contentType.toLowerCase();
        return !normalizedContentType.startsWith(MediaType.APPLICATION_JSON_VALUE);
    }

    private BaseRequest extractFromHeadersOrParameters(HttpServletRequest request) {
        String requestId = firstNonBlank(
                request.getHeader(RequestAttributes.REQUEST_ID),
                headerAny(request, List.of("RT-REQUEST-ID", "X-Request-Id")),
                request.getParameter("requestId")
        );
        String requestDateTime = firstNonBlank(
                request.getHeader(RequestAttributes.REQUEST_DATE_TIME),
                headerAny(request, List.of("RT-REQUEST-DATE-TIME", "X-Request-DateTime")),
                request.getParameter("requestDateTime")
        );
        String channel = firstNonBlank(
                request.getHeader(RequestAttributes.CHANNEL),
                headerAny(request, List.of("X-Channel")),
                request.getParameter("channel")
        );

        if (requestId == null || requestDateTime == null || channel == null) {
            throw new IllegalArgumentException("Missing request envelope headers");
        }

        return BaseRequest.builder()
                .requestId(requestId)
                .requestDateTime(requestDateTime)
                .channel(channel)
                .build();
    }

    private String headerAny(HttpServletRequest request, List<String> names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
