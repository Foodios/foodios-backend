package vn.com.orchestration.foodios.service.wallet.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.wallet.GetWalletTransactionsResponse;
import vn.com.orchestration.foodios.dto.wallet.TopUpRequest;
import vn.com.orchestration.foodios.dto.wallet.WalletResponse;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.wallet.Wallet;
import vn.com.orchestration.foodios.entity.wallet.WalletTransaction;
import vn.com.orchestration.foodios.entity.wallet.WalletTransactionStatus;
import vn.com.orchestration.foodios.entity.wallet.WalletTransactionType;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.repository.WalletRepository;
import vn.com.orchestration.foodios.repository.WalletTransactionRepository;
import vn.com.orchestration.foodios.service.wallet.WalletService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static vn.com.orchestration.foodios.constant.ErrorConstant.ADMIN_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final vn.com.orchestration.foodios.repository.OrderRepository orderRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    @Override
    @Transactional
    public WalletResponse getOrCreateWallet(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        UUID userId = UUID.fromString(currentUser.subject());

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> createNewWallet(userId));

        return mapToResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public GetWalletTransactionsResponse getTransactions(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        UUID userId = UUID.fromString(currentUser.subject());

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> createNewWallet(userId));

        List<WalletTransaction> transactions = walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());

        return GetWalletTransactionsResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetWalletTransactionsResponse.GetWalletTransactionsResponseData.builder()
                        .items(transactions.stream().map(this::mapToTransactionPayload).collect(Collectors.toList()))
                        .totalItems((long) transactions.size())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetWalletTransactionsResponse getAllTransactions(BaseRequest request) {
        authorizeAdmin(request);

        List<WalletTransaction> transactions = walletTransactionRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());

        BigDecimal totalNetRevenue = BigDecimal.ZERO;
        BigDecimal totalPayout = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;

        List<GetWalletTransactionsResponse.TransactionPayload> itemPayloads = transactions.stream()
                .map(this::mapToTransactionPayload)
                .collect(Collectors.toList());

        for (GetWalletTransactionsResponse.TransactionPayload item : itemPayloads) {
            if (item.getNetRevenue() != null) totalNetRevenue = totalNetRevenue.add(item.getNetRevenue());
            if (item.getPayout() != null) totalPayout = totalPayout.add(item.getPayout());
            if (item.getCommission() != null) totalCommission = totalCommission.add(item.getCommission());
        }

        return GetWalletTransactionsResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetWalletTransactionsResponse.GetWalletTransactionsResponseData.builder()
                        .items(itemPayloads)
                        .totalItems((long) transactions.size())
                        .netRevenue(totalNetRevenue)
                        .totalPayout(totalPayout)
                        .totalCommission(totalCommission)
                        .build())
                .build();
    }

    private GetWalletTransactionsResponse.TransactionPayload mapToTransactionPayload(WalletTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        BigDecimal netRevenue = BigDecimal.ZERO;
        BigDecimal payout = BigDecimal.ZERO;
        BigDecimal commission = BigDecimal.ZERO;

        if (transaction.getType() == WalletTransactionType.TOP_UP) {
            netRevenue = amount; // For Admin, Top-up count as influx
        } else if (transaction.getType() == WalletTransactionType.PAYMENT && transaction.getReferenceId() != null) {
            try {
                UUID orderId = UUID.fromString(transaction.getReferenceId());
                FoodOrder order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    BigDecimal totalAmount = order.getTotal();
                    BigDecimal rate = order.getStore().getMerchant().getCommissionRate();
                    if (rate == null) rate = new BigDecimal("15.00"); // Default

                    commission = totalAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    netRevenue = commission; // System's revenue is the commission
                    payout = totalAmount.subtract(commission); // Remainder to merchant
                }
            } catch (Exception e) {
                // Ignore parsing errors for referenceId
            }
        }

        return GetWalletTransactionsResponse.TransactionPayload.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .userEmail(transaction.getWallet().getUser().getEmail())
                .fullName(transaction.getWallet().getUser().getFullName())
                .amount(amount)
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .netRevenue(netRevenue)
                .payout(payout)
                .commission(commission)
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE)
            );
        }
    }

    @Override
    @Transactional
    public WalletResponse topUp(TopUpRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        UUID userId = UUID.fromString(currentUser.subject());

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> createNewWallet(userId));

        BigDecimal amount = request.getData().getAmount();

        // 1. Update Wallet Balance
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // 2. Create Transaction Record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(WalletTransactionType.TOP_UP)
                .status(WalletTransactionStatus.SUCCESS)
                .description(request.getData().getDescription() != null ? 
                             request.getData().getDescription() : "Wallet Top-up")
                .build();
        walletTransactionRepository.save(transaction);

        return mapToResponse(wallet);
    }

    private Wallet createNewWallet(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
        return walletRepository.save(wallet);
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(WalletResponse.WalletData.builder()
                        .userId(wallet.getUser().getId())
                        .balance(wallet.getBalance())
                        .currency(wallet.getCurrency())
                        .status(wallet.getStatus().name())
                        .build())
                .build();
    }
}
