package vn.com.orchestration.foodios.service.promotion;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.promotion.ValidatePromotionResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustomerPromotionService {

    ValidatePromotionResponse validatePromotion(
            BaseRequest request,
            String code,
            UUID storeId,
            BigDecimal orderAmount
    );
}
