package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.category.CreateCategoryRequest;
import vn.com.orchestration.foodios.dto.category.CreateCategoryResponse;
import vn.com.orchestration.foodios.dto.category.DeleteCategoryResponse;
import vn.com.orchestration.foodios.dto.category.GetCategoriesResponse;
import vn.com.orchestration.foodios.dto.category.GetCategoryResponse;
import vn.com.orchestration.foodios.dto.category.UpdateCategoryRequest;
import vn.com.orchestration.foodios.dto.category.UpdateCategoryResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.catalog.MerchantCategoryService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.CATEGORIES_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.CREATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH)
@RequiredArgsConstructor
public class MerchantCategoryController {

    private final MerchantCategoryService merchantCategoryService;

    @PostMapping(CATEGORIES_PATH + CREATE_PATH)
    public ResponseEntity<CreateCategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryResponse response = merchantCategoryService.createCategory(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PutMapping(CATEGORIES_PATH + "/{categoryId}")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        UpdateCategoryResponse response = merchantCategoryService.updateCategory(categoryId, request);
        return HttpUtils.buildResponse(request, response);
    }

    @DeleteMapping(CATEGORIES_PATH + "/{categoryId}")
    public ResponseEntity<DeleteCategoryResponse> deleteCategory(
            @PathVariable UUID categoryId,
            @RequestParam UUID storeId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteCategoryResponse response = merchantCategoryService.deleteCategory(categoryId, baseRequest, storeId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(CATEGORIES_PATH + "/{categoryId}")
    public ResponseEntity<GetCategoryResponse> getCategory(
            @PathVariable UUID categoryId,
            @RequestParam UUID storeId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCategoryResponse response = merchantCategoryService.getCategory(categoryId, baseRequest, storeId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(CATEGORIES_PATH)
    public ResponseEntity<GetCategoriesResponse> getCategories(
            @RequestParam UUID storeId,
            @RequestParam(required = false) CategoryStatus status,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCategoriesResponse response = merchantCategoryService.getCategories(baseRequest, storeId, status, activeOnly);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
