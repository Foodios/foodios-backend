package vn.com.orchestration.foodios.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.log.SystemLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static vn.com.orchestration.foodios.constant.ErrorConstant.AUTHORITIES_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.AUTHORIZATION_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_HTTP_REQUEST_RESOURCE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_HTTP_REQUEST_RESOURCE_ERROR_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_REQUEST_TIMESTAMP;
import static vn.com.orchestration.foodios.constant.ErrorConstant.TIMEOUT_ERROR;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    private BaseResponse buildBaseResponse(BaseRequest baseRequest, ApiResult result) {
        return BaseResponse.builder()
                .requestId(baseRequest.getRequestId())
                .requestDateTime(baseRequest.getRequestDateTime())
                .channel(baseRequest.getChannel())
                .result(result)
                .build();
    }

    private ResponseEntity<BaseResponse> createErrorResponse(
            HttpStatus status,
            BaseRequest baseRequest,
            String responseCode,
            String description
    ) {
        ApiResult result = ApiResult.builder()
                .responseCode(responseCode)
                .description(description)
                .build();

        return ResponseEntity.status(status)
                .body(buildBaseResponse(baseRequest, result));
    }

    private BaseRequest logAndGetBaseRequest(HttpServletRequest request, Exception e) {
        if (Objects.nonNull(e)) {
            sLog.error(ExceptionUtils.getStackTrace(e));
        }
        return ResponseUtil.getBaseRequestOrDefault(request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<BaseResponse> handleAuthorizationDeniedException(
            HttpServletRequest request,
            AuthorizationDeniedException exception
    ) {
        BaseRequest baseRequest = logAndGetBaseRequest(request, exception);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBaseResponse(
                        baseRequest,
                        ApiResult.builder()
                                .responseCode(AUTHORIZATION_ERROR)
                                .description(AUTHORITIES_ERROR)
                                .build()
                ));
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse> handleBusinessException(HttpServletRequest request, BusinessException ex) {
        BaseRequest baseRequest = logAndGetBaseRequest(request, ex);

        if (TIMEOUT_ERROR.equals(ex.getResult().getResponseCode())) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(buildBaseResponse(baseRequest, ex.getResult()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBaseResponse(baseRequest, ex.getResult()));

        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        String errorFieldList = getErrorFieldListResponse(ex.getBindingResult().getAllErrors());
        String errorMessage = "Invalid Input: " + errorFieldList;
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        String responseCode = getValidationResponseCode(ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, baseRequest, responseCode, errorMessage);
    }

    private static String getValidationResponseCode(MethodArgumentNotValidException e) {
        String responseCode = INVALID_INPUT_ERROR;

        if (e.getFieldError().getField().equals("channel") && e.getFieldError().getCode().equals("ValidChannelList")) {
            responseCode = INVALID_HTTP_REQUEST_RESOURCE_ERROR;
        }

        if (e.getFieldError().getField().equals("requestDateTime") && e.getFieldError().getCode().equals("ValidRequestDateTime")) {
            responseCode = INVALID_REQUEST_TIMESTAMP;
        }
        return responseCode;
    }
    private static String getFieldNameResponse(String fieldNameFullPath) {
        if (!fieldNameFullPath.contains(".")) {
            return fieldNameFullPath;
        }
        String[] fieldNameSplit = fieldNameFullPath.split("\\.");
        return fieldNameSplit[(fieldNameSplit.length - 1)];
    }

    private static String getErrorFieldListResponse(List<ObjectError> listError) {
        List<String> listField = new ArrayList<>();
        listError.forEach(errorObject -> {
            String fieldName = getFieldNameResponse(((FieldError) errorObject).getField());
            if (!listField.contains(fieldName)) {
                listField.add(fieldName);
            }
        });

        String result = listField.stream().map(field -> String.format("%s", field)).collect(Collectors.joining(","));
        if (result.length() > 83) {
            int lastCommaIndex = result.lastIndexOf(",");
            result = (lastCommaIndex != -1) ? result.substring(0, lastCommaIndex) + ",..." : result;
        }

        return result;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        BaseRequest baseRequest = logAndGetBaseRequest(request, e);
        return createErrorResponse(HttpStatus.BAD_REQUEST, baseRequest, INVALID_INPUT_ERROR, INVALID_INPUT_MESSAGE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

//    @ExceptionHandler(ValidationException.class)
//    public ResponseEntity<ApiResponse<Object>> handleValidationException(HttpServletRequest request, ValidationException e) {
//        ssLog.info("Business Exception: {}", e.getResult());
//        ApiRequest<?> baseRequest = logAndGetBaseRequest(request, e);
//        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(buildBaseResponse(baseRequest, e.getResult()));
//    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse> handleNoResourceFoundException(HttpServletRequest request, NoResourceFoundException e) {
        BaseRequest baseRequest = logAndGetBaseRequest(request, e);
        String description = String.format(INVALID_HTTP_REQUEST_RESOURCE_ERROR_MESSAGE, e.getResourcePath());
        return createErrorResponse(HttpStatus.BAD_REQUEST, baseRequest, INVALID_HTTP_REQUEST_RESOURCE_ERROR, description);
    }

}