package vn.com.orchestration.foodios.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.review.ReviewSourceType;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPayload {
    private UUID id;
    private UUID merchantId;
    private UUID storeId;
    private UUID orderId;
    private UUID customerId;
    private String customerName;
    private String customerAvatarUrl;
    private Integer rating;
    private String title;
    private String comment;
    private ReviewSourceType sourceType;
    private ReviewStatus status;
    private Instant reviewedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
