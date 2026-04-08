package vn.com.orchestration.foodios.service.user;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetFavoriteMerchantsResponse;
import vn.com.orchestration.foodios.dto.user.ToggleFavoriteRequest;
import vn.com.orchestration.foodios.dto.user.ToggleFavoriteResponse;

public interface UserFavoriteService {
    ToggleFavoriteResponse toggleFavorite(ToggleFavoriteRequest request);
    GetFavoriteMerchantsResponse getFavorites(BaseRequest request, Integer pageNumber, Integer pageSize);
}
