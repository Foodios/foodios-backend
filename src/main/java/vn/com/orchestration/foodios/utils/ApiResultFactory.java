package vn.com.orchestration.foodios.utils;

import org.springframework.stereotype.Component;
import vn.com.orchestration.foodios.dto.common.ApiResult;

import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Component
public class ApiResultFactory {
    public ApiResult buildSuccess() {
        return ApiResult.builder()
                .responseCode(SUCCESS_CODE)
                .description(SUCCESS_MESSAGE)
                .build();
    }
}
