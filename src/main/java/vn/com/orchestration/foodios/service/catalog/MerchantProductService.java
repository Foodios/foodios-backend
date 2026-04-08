package vn.com.orchestration.foodios.service.catalog;

import vn.com.orchestration.foodios.dto.product.CreateProductRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductResponse;

public interface MerchantProductService {
    CreateProductResponse createProduct(CreateProductRequest request);
}
