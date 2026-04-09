package vn.com.orchestration.foodios.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetWalletTransactionsResponse extends BaseResponse<GetWalletTransactionsResponse.GetWalletTransactionsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetWalletTransactionsResponseData {
        private List<TransactionPayload> items;
        private Long totalItems;
        private BigDecimal netRevenue;
        private BigDecimal totalPayout;
        private BigDecimal totalCommission;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionPayload {
        private UUID id;
        private UUID walletId;
        private String userEmail;
        private String fullName;
        private BigDecimal amount;
        private String type;
        private String status;
        private String description;
        private String referenceId;
        private BigDecimal netRevenue;
        private BigDecimal payout;
        private BigDecimal commission;
        private OffsetDateTime createdAt;
    }
}
