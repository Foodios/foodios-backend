package vn.com.orchestration.foodios.service.cart;

import vn.com.orchestration.foodios.dto.cart.AddCartItemRequest;
import vn.com.orchestration.foodios.dto.cart.AddCartItemResponse;
import vn.com.orchestration.foodios.dto.cart.GetCartResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.util.UUID;

public interface CustomerCartService {

    AddCartItemResponse addItem(AddCartItemRequest request);

    GetCartResponse getCart(BaseRequest request, UUID storeId);
}
