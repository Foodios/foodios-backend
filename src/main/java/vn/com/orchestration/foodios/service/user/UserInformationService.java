package vn.com.orchestration.foodios.service.user;

import jakarta.validation.Valid;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetMyProfileResponse;
import vn.com.orchestration.foodios.dto.user.UpdateProfileRequest;
import vn.com.orchestration.foodios.dto.user.UpdateProfileResponse;

public interface UserInformationService {

    GetMyProfileResponse getMyProfile(BaseRequest request, String userId);

    UpdateProfileResponse updateProfile(UpdateProfileRequest request);
}
