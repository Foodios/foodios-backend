package vn.com.orchestration.foodios.service.search;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.search.GlobalSearchResponse;

public interface GlobalSearchService {
    GlobalSearchResponse search(BaseRequest request, String query);
}
