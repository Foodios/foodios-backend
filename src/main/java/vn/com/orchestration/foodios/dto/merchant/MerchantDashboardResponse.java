package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MerchantDashboardResponse extends BaseResponse<MerchantDashboardResponse.MerchantDashboardData> {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MerchantDashboardData {
        private SummaryCards summary;
        private RevenueAnalytics analytics;
        private SystemStatus systemStatus;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SummaryCards {
        private MetricValue dailyRevenue;
        private MetricValue totalOrders;
        private MetricValue activeCustomers;
        private MetricValue merchantRating;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MetricValue {
        private String value;
        private Double changePercentage;
        private Boolean isIncrease;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RevenueAnalytics {
        private List<String> labels;
        private List<BigDecimal> onlineData;
        private List<BigDecimal> posData;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SystemStatus {
        private Double efficiency;
        private Integer waitTime;
    }
}
