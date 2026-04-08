package vn.com.orchestration.foodios.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import vn.com.orchestration.foodios.config.RequestAttributes;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.util.List;


@UtilityClass
public class ResponseUtil {
    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private String headerAny(HttpServletRequest request, List<String> names) {
        for (String n : names) {
            String v = request.getHeader(n);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    public BaseRequest getBaseRequestOrDefault(HttpServletRequest request) {
        String requestId =
                firstNonBlank(
                        (String) request.getAttribute(RequestAttributes.REQUEST_ID),
                        headerAny(request, List.of(RequestAttributes.REQUEST_ID, "RT-REQUEST-ID", "X-Request-Id")),
                        request.getParameter("requestId")
                );
        String requestDateTime =
                firstNonBlank(
                        (String) request.getAttribute(RequestAttributes.REQUEST_DATE_TIME),
                        headerAny(request, List.of(RequestAttributes.REQUEST_DATE_TIME, "RT-REQUEST-DATE-TIME", "X-Request-DateTime")),
                        request.getParameter("requestDateTime")
                );
        String channel =
                firstNonBlank(
                        (String) request.getAttribute(RequestAttributes.CHANNEL),
                        headerAny(request, List.of(RequestAttributes.CHANNEL, "X-Channel")),
                        request.getParameter("channel")
                );

        return BaseRequest.builder()
                .requestId(requestId)
                .requestDateTime(requestDateTime)
                .channel(channel)
                .build();
    }
}

