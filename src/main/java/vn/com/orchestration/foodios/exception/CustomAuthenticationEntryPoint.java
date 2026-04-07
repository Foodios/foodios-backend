package vn.com.orchestration.foodios.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {

        BaseRequest baseRequest = extractBaseRequest(request);

        BaseResponse body = buildBaseResponse(
                baseRequest,
                ApiResult.builder()
                        .responseCode("3100")
                        .description("Authentication required or token is invalid")
                        .build()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), body);
    }

    private BaseRequest extractBaseRequest(HttpServletRequest request) {
        Object requestBody = request.getAttribute("baseRequest");
        if (requestBody instanceof BaseRequest baseRequest) {
            return baseRequest;
        }

        return BaseRequest.builder()
                .requestId(request.getHeader("X-Request-Id"))
                .channel(request.getHeader("X-Channel"))
                .build();
    }

    private BaseResponse buildBaseResponse(BaseRequest baseRequest, ApiResult result) {
        return BaseResponse.builder()
                .requestId(baseRequest != null ? baseRequest.getRequestId() : null)
                .requestDateTime(baseRequest != null ? baseRequest.getRequestDateTime() : null)
                .channel(baseRequest != null ? baseRequest.getChannel() : null)
                .result(result)
                .build();
    }
}