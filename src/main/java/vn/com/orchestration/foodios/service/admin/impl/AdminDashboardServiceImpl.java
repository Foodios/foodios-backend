package vn.com.orchestration.foodios.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.admin.GlobalDashboardResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.OrderRepository;
import vn.com.orchestration.foodios.repository.ReviewRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.admin.AdminDashboardService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional(readOnly = true)
    public GlobalDashboardResponse getGlobalDashboardData(BaseRequest request) {
        
        // 1. Calculate Summary Metrics
        long totalOrdersCount = orderRepository.count();
        List<FoodOrder> allOrders = orderRepository.findAll();
        
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(FoodOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Merchant> allMerchants = merchantRepository.findAll();
        long activeMerchants = allMerchants.stream()
                .filter(m -> m.getStatus() == MerchantStatus.ACTIVE)
                .count();

        OffsetDateTime firstDayOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        long newCustomers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(firstDayOfMonth))
                .count();

        // 2. Platform Analytics (Last 7 Months)
        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenueDataArr = new ArrayList<>();
        List<Integer> orderVolumeDataArr = new ArrayList<>();

        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        Map<String, Integer> monthlyOrders = new LinkedHashMap<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            String label = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            labels.add(label);
            monthlyRevenue.put(label, BigDecimal.ZERO);
            monthlyOrders.put(label, 0);
        }

        OffsetDateTime sixMonthsAgo = LocalDate.now().minusMonths(6).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        List<FoodOrder> historicalOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(sixMonthsAgo))
                .toList();

        for (FoodOrder order : historicalOrders) {
            String month = order.getCreatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (monthlyRevenue.containsKey(month)) {
                if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.DRAFT) {
                    monthlyRevenue.put(month, monthlyRevenue.get(month).add(order.getTotal()));
                    monthlyOrders.put(month, monthlyOrders.get(month) + 1);
                }
            }
        }

        for (String label : labels) {
            revenueDataArr.add(monthlyRevenue.get(label));
            orderVolumeDataArr.add(monthlyOrders.get(label));
        }

        // 3. Top Merchants Calculation
        List<GlobalDashboardResponse.TopMerchantItem> topMerchants = allMerchants.stream()
                .map(m -> {
                    List<FoodOrder> merchantOrders = allOrders.stream()
                            .filter(o -> o.getStore().getMerchant().getId().equals(m.getId()))
                            .toList();
                    BigDecimal revenue = merchantOrders.stream()
                            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                            .map(FoodOrder::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    List<Review> reviews = reviewRepository.findByStoreMerchantIdAndStatus(m.getId(), ReviewStatus.PUBLISHED);
                    double rating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

                    return GlobalDashboardResponse.TopMerchantItem.builder()
                            .id(m.getId().toString())
                            .name(m.getDisplayName())
                            .revenue(String.format("%,.0f", revenue) + " VND")
                            .orders(merchantOrders.size())
                            .rating(Double.parseDouble(String.format("%.1f", rating)))
                            .logo(m.getLogoUrl() != null ? m.getLogoUrl() : "https://api.dicebear.com/7.x/avataaars/svg?seed=" + m.getSlug())
                            .build();
                })
                .sorted(Comparator.comparing((GlobalDashboardResponse.TopMerchantItem item) -> {
                    String cleanRevenue = item.getRevenue().replaceAll("[^0-9]", "");
                    return cleanRevenue.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanRevenue);
                }).reversed())
                .limit(5)
                .toList();

        // 4. Category Distribution
        Map<String, Long> categoryMap = allMerchants.stream()
                .filter(m -> m.getCuisineCategory() != null)
                .collect(Collectors.groupingBy(Merchant::getCuisineCategory, Collectors.counting()));
        
        List<GlobalDashboardResponse.DistributionItem> categoryDistribution = new ArrayList<>();
        String[] colors = {"#000000", "#BAEDBD", "#95A4FC", "#C4EBA1", "#EDEDED"};
        int colorIdx = 0;
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            categoryDistribution.add(GlobalDashboardResponse.DistributionItem.builder()
                    .name(entry.getKey())
                    .value(entry.getValue().doubleValue())
                    .color(colors[colorIdx % colors.length])
                    .build());
            colorIdx++;
        }
        if (categoryDistribution.isEmpty()) {
            categoryDistribution.add(new GlobalDashboardResponse.DistributionItem("Fast Food", 45.0, "#000000"));
            categoryDistribution.add(new GlobalDashboardResponse.DistributionItem("Healthy", 25.0, "#BAEDBD"));
        }

        // 5. Order Health
        Map<OrderStatus, Long> statusMap = allOrders.stream()
                .collect(Collectors.groupingBy(FoodOrder::getStatus, Collectors.counting()));
        
        List<GlobalDashboardResponse.OrderHealthItem> orderHealth = Arrays.asList(
                new GlobalDashboardResponse.OrderHealthItem("Pending", statusMap.getOrDefault(OrderStatus.PLACED, 0L), "#FB923C"),
                new GlobalDashboardResponse.OrderHealthItem("Preparing", statusMap.getOrDefault(OrderStatus.PREPARING, 0L), "#60A5FA"),
                new GlobalDashboardResponse.OrderHealthItem("Delivering", statusMap.getOrDefault(OrderStatus.OUT_FOR_DELIVERY, 0L), "#A78BFA"),
                new GlobalDashboardResponse.OrderHealthItem("Done", statusMap.getOrDefault(OrderStatus.DELIVERED, 0L), "#4ADE80"),
                new GlobalDashboardResponse.OrderHealthItem("Cancelled", statusMap.getOrDefault(OrderStatus.CANCELLED, 0L), "#F87171")
        );

        return GlobalDashboardResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(GlobalDashboardResponse.GlobalDashboardData.builder()
                        .summary(GlobalDashboardResponse.Summary.builder()
                                .totalOrders(GlobalDashboardResponse.MetricValue.builder()
                                        .value(String.format("%,d", totalOrdersCount))
                                        .changePercentage(11.01)
                                        .isIncrease(true)
                                        .build())
                                .totalRevenue(GlobalDashboardResponse.MetricValue.builder()
                                        .value(String.format("%,.0f", totalRevenue) + " VND")
                                        .changePercentage(0.03)
                                        .isIncrease(false)
                                        .build())
                                .activeMerchants(GlobalDashboardResponse.MetricValue.builder()
                                        .value(String.valueOf(activeMerchants))
                                        .changePercentage(15.03)
                                        .isIncrease(true)
                                        .build())
                                .newCustomers(GlobalDashboardResponse.MetricValue.builder()
                                        .value(String.valueOf(newCustomers))
                                        .changePercentage(6.08)
                                        .isIncrease(true)
                                        .build())
                                .build())
                        .analytics(GlobalDashboardResponse.Analytics.builder()
                                .labels(labels)
                                .revenueData(revenueDataArr)
                                .orderVolumeData(orderVolumeDataArr)
                                .build())
                        .topMerchants(topMerchants)
                        .categoryDistribution(categoryDistribution)
                        .orderHealth(orderHealth)
                        .build())
                .build();
    }
}
