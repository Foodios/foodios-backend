package vn.com.orchestration.foodios.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryPayload {
    private double averageRating;
    private long totalReviews;
    private long fiveStarCount;
    private long fourStarCount;
    private long threeStarCount;
    private long twoStarCount;
    private long oneStarCount;
}
