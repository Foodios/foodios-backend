package vn.com.orchestration.foodios.service.media;

import vn.com.orchestration.foodios.dto.media.UploadMediaRequest;
import vn.com.orchestration.foodios.dto.media.UploadMediaResponse;

public interface MediaService {
    UploadMediaResponse uploadImage(UploadMediaRequest request);
}
