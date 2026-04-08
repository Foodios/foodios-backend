package vn.com.orchestration.foodios.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UploadMediaResponse extends BaseResponse<UploadMediaResponse.UploadMediaResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadMediaResponseData {
        private String publicId;
        private String originalFilename;
        private String resourceType;
        private String format;
        private String url;
        private String secureUrl;
        private String folder;
        private Long bytes;
        private Integer width;
        private Integer height;
    }
}
