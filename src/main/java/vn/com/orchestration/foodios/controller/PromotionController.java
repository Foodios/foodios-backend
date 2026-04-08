package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.promotion.ValidatePromotionResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.promotion.CustomerPromotionService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.PROMOTIONS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.VALIDATE_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + PROMOTIONS_PATH)
@RequiredArgsConstructor
public class PromotionController {

    private final CustomerPromotionService customerPromotionService;

    @GetMapping(VALIDATE_PATH)
    public ResponseEntity<ValidatePromotionResponse> validatePromotion(
            @RequestParam String code,
            @RequestParam UUID storeId,
            @RequestParam BigDecimal orderAmount,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        ValidatePromotionResponse response =
                customerPromotionService.validatePromotion(baseRequest, code, storeId, orderAmount);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
