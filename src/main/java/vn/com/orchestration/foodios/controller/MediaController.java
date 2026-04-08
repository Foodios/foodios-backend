package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.media.UploadMediaRequest;
import vn.com.orchestration.foodios.dto.media.UploadMediaResponse;
import vn.com.orchestration.foodios.service.media.MediaService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MEDIA_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.UPLOAD_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MEDIA_PATH)
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(path = UPLOAD_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadMediaResponse> upload(@Valid @ModelAttribute UploadMediaRequest request) {
        UploadMediaResponse response = mediaService.uploadImage(request);
        return HttpUtils.buildResponse(request, response);
    }
}
