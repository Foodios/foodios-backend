package vn.com.orchestration.foodios.service.catalog;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductResponse;
import vn.com.orchestration.foodios.dto.product.DeleteProductResponse;
import vn.com.orchestration.foodios.dto.product.GetProductResponse;
import vn.com.orchestration.foodios.dto.product.GetProductsResponse;
import vn.com.orchestration.foodios.dto.product.UpdateProductRequest;
import vn.com.orchestration.foodios.dto.product.UpdateProductResponse;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;

import java.util.UUID;

public interface MerchantProductService {
    CreateProductResponse createProduct(CreateProductRequest request);

    UpdateProductResponse updateProduct(UUID productId, UpdateProductRequest request);

    DeleteProductResponse deleteProduct(UUID productId, BaseRequest request, UUID storeId);

    GetProductResponse getProduct(UUID productId, BaseRequest request, UUID storeId);

    GetProductsResponse getProducts(BaseRequest request, UUID storeId, UUID categoryId, ProductStatus status);
}
