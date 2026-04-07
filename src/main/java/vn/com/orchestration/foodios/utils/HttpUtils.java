package vn.com.orchestration.foodios.utils;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.exception.BusinessException;

import static vn.com.orchestration.foodios.constant.ErrorConstant.TIMEOUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.TIMEOUT_ERROR_MESSAGE;


@UtilityClass
public class HttpUtils {

    public <T, R extends BaseResponse<T>> ResponseEntity<R> buildResponse(BaseRequest request, R response) {
        if (response == null) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(TIMEOUT_ERROR, TIMEOUT_ERROR_MESSAGE)
            );
        }

        response.setRequestId(request.getRequestId());
        response.setRequestDateTime(request.getRequestDateTime());
        response.setChannel(request.getChannel());

        return ResponseEntity
                .status(response.getData() == null ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
