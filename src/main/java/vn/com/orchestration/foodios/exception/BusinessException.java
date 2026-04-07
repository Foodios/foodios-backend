package vn.com.orchestration.foodios.exception;

import lombok.EqualsAndHashCode;
import vn.com.orchestration.foodios.dto.common.ApiResult;

@EqualsAndHashCode(callSuper = true)
public class BusinessException extends BaseException {

    public BusinessException(String requestId, String requestDateTime, String channel, ApiResult result) {
        super(requestId, requestDateTime, channel, result);
    }

    public BusinessException(ApiResult result) { super(result); }
}
