package vn.com.orchestration.foodios.service.catalog;

import vn.com.orchestration.foodios.dto.category.CreateCategoryRequest;
import vn.com.orchestration.foodios.dto.category.CreateCategoryResponse;
import vn.com.orchestration.foodios.dto.category.DeleteCategoryResponse;
import vn.com.orchestration.foodios.dto.category.GetCategoriesResponse;
import vn.com.orchestration.foodios.dto.category.GetCategoryResponse;
import vn.com.orchestration.foodios.dto.category.UpdateCategoryRequest;
import vn.com.orchestration.foodios.dto.category.UpdateCategoryResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;

import java.util.UUID;

public interface MerchantCategoryService {

    CreateCategoryResponse createCategory(CreateCategoryRequest request);

    UpdateCategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request);

    DeleteCategoryResponse deleteCategory(UUID categoryId, BaseRequest request, UUID storeId);

    GetCategoryResponse getCategory(UUID categoryId, BaseRequest request, UUID storeId);

    GetCategoriesResponse getCategories(BaseRequest request, UUID storeId, CategoryStatus status, boolean activeOnly);
}
