package vn.com.orchestration.foodios.dto.admin;

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
public class GlobalDashboardResponse extends BaseResponse<GlobalDashboardResponse.GlobalDashboardData> {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GlobalDashboardData {
        private Summary summary;
        private Analytics analytics;
        private List<TopMerchantItem> topMerchants;
        private List<DistributionItem> categoryDistribution;
        private List<OrderHealthItem> orderHealth;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private MetricValue totalOrders;
        private MetricValue totalRevenue;
        private MetricValue activeMerchants;
        private MetricValue newCustomers;
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
    public static class Analytics {
        private List<String> labels;
        private List<BigDecimal> revenueData;
        private List<Integer> orderVolumeData;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopMerchantItem {
        private String id;
        private String name;
        private String revenue;
        private Integer orders;
        private Double rating;
        private String logo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DistributionItem {
        private String name;
        private Double value;
        private String color;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderHealthItem {
        private String label;
        private Long count;
        private String color;
    }
}
