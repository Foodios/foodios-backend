package vn.com.orchestration.foodios.service.media.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.orchestration.foodios.config.CloudinaryProperties;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.media.UploadMediaRequest;
import vn.com.orchestration.foodios.dto.media.UploadMediaResponse;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.service.media.MediaService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.io.IOException;
import java.util.Map;

import static vn.com.orchestration.foodios.constant.ErrorConstant.FILE_UPLOAD_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.FILE_UPLOAD_ERROR_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_FILE_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    @Transactional(readOnly = true)
    public UploadMediaResponse uploadImage(UploadMediaRequest request) {
        UploadMediaRequest.UploadMediaRequestData data = request.getData();
        if (data == null || data.getFile() == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        MultipartFile file = data.getFile();
        validateImageFile(request, file);
        validateCloudinaryConfiguration(request);

        String folder = resolveFolder(data.getFolder());

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder,
                            "use_filename", Boolean.TRUE,
                            "unique_filename", Boolean.TRUE,
                            "overwrite", Boolean.FALSE,
                            "public_id", resolvePublicId(data.getPublicId())
                    )
            );

            UploadMediaResponse response = new UploadMediaResponse();
            response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
            response.setData(
                    UploadMediaResponse.UploadMediaResponseData.builder()
                            .publicId(stringValue(uploadResult, "public_id"))
                            .originalFilename(resolveOriginalFilename(file, uploadResult))
                            .resourceType(stringValue(uploadResult, "resource_type"))
                            .format(stringValue(uploadResult, "format"))
                            .url(stringValue(uploadResult, "url"))
                            .secureUrl(stringValue(uploadResult, "secure_url"))
                            .folder(folder)
                            .bytes(longValue(uploadResult, "bytes"))
                            .width(integerValue(uploadResult, "width"))
                            .height(integerValue(uploadResult, "height"))
                            .build()
            );
            return response;
        } catch (IOException exception) {
            sLog.error("[MEDIA_UPLOAD] requestId={} upload failed", request.getRequestId(), exception);
            throw businessException(request, FILE_UPLOAD_ERROR, FILE_UPLOAD_ERROR_MESSAGE);
        }
    }

    private void validateImageFile(BaseRequest request, MultipartFile file) {
        if (file.isEmpty()) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_FILE_MESSAGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_FILE_MESSAGE);
        }
    }

    private void validateCloudinaryConfiguration(BaseRequest request) {
        if (isBlank(cloudinaryProperties.getCloudName())
                || isBlank(cloudinaryProperties.getApiKey())
                || isBlank(cloudinaryProperties.getApiSecret())) {
            throw businessException(request, FILE_UPLOAD_ERROR, "Cloudinary is not configured");
        }
    }

    private String resolveFolder(String folder) {
        if (!isBlank(folder)) {
            return folder.trim();
        }
        if (!isBlank(cloudinaryProperties.getDefaultFolder())) {
            return cloudinaryProperties.getDefaultFolder().trim();
        }
        return "foodios";
    }

    private String resolvePublicId(String publicId) {
        if (isBlank(publicId)) {
            return null;
        }
        return publicId.trim();
    }

    private String resolveOriginalFilename(MultipartFile file, Map<?, ?> uploadResult) {
        String originalFilename = stringValue(uploadResult, "original_filename");
        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename;
        }
        return file.getOriginalFilename();
    }

    private String stringValue(Map<?, ?> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : value.toString();
    }

    private Long longValue(Map<?, ?> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Integer integerValue(Map<?, ?> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
