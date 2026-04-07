package vn.com.orchestration.foodios.utils;


import lombok.experimental.UtilityClass;
import vn.com.orchestration.foodios.dto.common.ApiResult;

@UtilityClass
public class ExceptionUtils {

    public ApiResult buildResultResponse(String responseCode, String description) {
        return ApiResult
                .builder()
                .responseCode(responseCode)
                .description(description)
                .build();
    }
}
