package vn.com.orchestration.foodios.service.order.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.order.GetOrderResponse;
import vn.com.orchestration.foodios.dto.order.GetOrdersResponse;
import vn.com.orchestration.foodios.dto.order.OrderItemPayload;
import vn.com.orchestration.foodios.dto.order.OrderPayload;
import vn.com.orchestration.foodios.dto.order.PlaceOrderRequest;
import vn.com.orchestration.foodios.dto.order.PlaceOrderResponse;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderItem;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.entity.order.Payment;
import vn.com.orchestration.foodios.entity.order.PaymentMethod;
import vn.com.orchestration.foodios.entity.order.PaymentStatus;
import vn.com.orchestration.foodios.entity.order.ServiceMethod;
import vn.com.orchestration.foodios.entity.promotion.Coupon;
import vn.com.orchestration.foodios.entity.promotion.CouponRedemption;
import vn.com.orchestration.foodios.entity.promotion.CouponScope;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.entity.promotion.DiscountType;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.CouponRedemptionRepository;
import vn.com.orchestration.foodios.repository.CouponRepository;
import vn.com.orchestration.foodios.repository.OrderItemRepository;
import vn.com.orchestration.foodios.repository.OrderRepository;
import vn.com.orchestration.foodios.repository.PaymentRepository;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.repository.WalletRepository;
import vn.com.orchestration.foodios.repository.WalletTransactionRepository;
import vn.com.orchestration.foodios.repository.CustomerMembershipRepository;
import vn.com.orchestration.foodios.entity.wallet.Wallet;
import vn.com.orchestration.foodios.entity.wallet.WalletTransaction;
import vn.com.orchestration.foodios.entity.wallet.WalletTransactionStatus;
import vn.com.orchestration.foodios.entity.wallet.WalletTransactionType;
import vn.com.orchestration.foodios.service.order.CustomerOrderService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_NUMBER;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_SIZE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final UserRepository userRepository;
    private final CustomerMembershipRepository customerMembershipRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        PlaceOrderRequest.PlaceOrderRequestData data = requireData(request, request.getData());
        User customer = resolveCurrentUser(request);

        Store store = storeRepository.findById(data.getStoreId())
                .filter(item -> item.getStatus() == StoreStatus.ACTIVE)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));

        ServiceMethod serviceMethod = resolveServiceMethod(data.getServiceMethod(), request);
        PaymentMethod paymentMethod = resolvePaymentMethod(data.getPaymentMethod(), request);
        String currency = resolveCurrency(data.getCurrency());

        if (serviceMethod == ServiceMethod.DELIVERY && data.getShippingAddress() == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Shipping address is required for delivery orders");
        }

        List<ResolvedOrderItem> resolvedItems = data.getItems().stream()
                .map(item -> resolveOrderItem(request, store, item))
                .toList();

        BigDecimal subtotal = resolvedItems.stream()
                .map(ResolvedOrderItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFee = serviceMethod == ServiceMethod.DELIVERY ? BigDecimal.ZERO : BigDecimal.ZERO;
        BigDecimal serviceFee = BigDecimal.ZERO;

        Coupon coupon = resolveCoupon(data.getPromoCode(), store, subtotal, customer.getId(), request);
        BigDecimal discountAmount = calculateDiscount(coupon, subtotal);
        BigDecimal total = subtotal.add(deliveryFee).add(serviceFee).subtract(discountAmount).max(BigDecimal.ZERO);

        if (data.getTotalAmount() != null && total.compareTo(data.getTotalAmount()) != 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Total amount does not match calculated total");
        }

        FoodOrder order = FoodOrder.builder()
                .code(generateOrderCode())
                .store(store)
                .customer(customer)
                .status(OrderStatus.PLACED)
                .serviceMethod(serviceMethod)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .serviceFee(serviceFee)
                .discountAmount(discountAmount)
                .total(total)
                .currency(currency)
                .appliedCouponCode(coupon != null ? coupon.getCode() : null)
                .notes(trimToNull(data.getNotes()))
                .placedAt(Instant.now())
                .deliveryAddress(toDeliveryAddress(serviceMethod, data.getShippingAddress()))
                .build();
        FoodOrder savedOrder = orderRepository.saveAndFlush(order);

        List<OrderItem> orderItems = resolvedItems.stream()
                .map(item -> OrderItem.builder()
                        .order(savedOrder)
                        .product(item.product())
                        .productName(item.product().getName())
                        .quantity(item.quantity())
                        .unitPrice(item.unitPrice())
                        .totalPrice(item.totalPrice())
                        .build())
                .map(OrderItem.class::cast)
                .toList();
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);

        PaymentStatus paymentStatus = resolvePaymentStatus(paymentMethod);
        if (paymentMethod == PaymentMethod.E_WALLET) {
            processWalletPayment(customer, total, savedOrder, request);
            paymentStatus = PaymentStatus.PAID;
            awardLoyaltyPoints(customer, orderItems);
        }

        Payment payment = Payment.builder()
                .order(savedOrder)
                .method(paymentMethod)
                .status(paymentStatus)
                .amount(total)
                .provider(resolvePaymentProvider(paymentMethod))
                .paidAt(paymentStatus == PaymentStatus.PAID ? Instant.now() : null)
                .build();
        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        if (coupon != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            couponRedemptionRepository.saveAndFlush(CouponRedemption.builder()
                    .coupon(coupon)
                    .user(customer)
                    .order(savedOrder)
                    .discountAmount(discountAmount)
                    .build());
        }

        return PlaceOrderResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedOrder, savedItems, savedPayment))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderResponse getOrder(BaseRequest request, UUID orderId) {
        User customer = resolveCurrentUser(request);
        FoodOrder order = orderRepository.findById(orderId)
                .filter(item -> item.getCustomer().getId().equals(customer.getId()))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Payment not found"));

        return GetOrderResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(order, items, payment))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrdersResponse getOrders(BaseRequest request, OrderStatus status, Integer pageNumber, Integer pageSize) {
        validatePagination(request, pageNumber, pageSize);
        User customer = resolveCurrentUser(request);

        PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FoodOrder> orders = status == null
                ? orderRepository.findByCustomerId(customer.getId(), pageable)
                : orderRepository.findByCustomerIdAndStatus(customer.getId(), status, pageable);

        return GetOrdersResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetOrdersResponse.GetOrdersResponseData.builder()
                        .items(orders.getContent().stream().map(this::toPayload).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(orders.getTotalElements())
                        .totalPages(orders.getTotalPages())
                        .hasNext(orders.hasNext())
                        .build())
                .build();
    }

    private ResolvedOrderItem resolveOrderItem(
            BaseRequest request,
            Store store,
            PlaceOrderRequest.OrderItemInput item
    ) {
        var product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Product not found"));
        if (!product.getStore().getId().equals(store.getId())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Product does not belong to store");
        }
        if (product.getStatus() != vn.com.orchestration.foodios.entity.catalog.ProductStatus.ACTIVE || !product.isAvailable()) {
            throw businessException(request, INVALID_INPUT_ERROR, "Product is not available");
        }
        if (product.getPrice().compareTo(item.getUnitPrice()) != 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Product price has changed");
        }
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new ResolvedOrderItem(product, item.getQuantity(), product.getPrice(), totalPrice);
    }

    private Coupon resolveCoupon(
            String rawPromoCode,
            Store store,
            BigDecimal subtotal,
            UUID userId,
            BaseRequest request
    ) {
        String promoCode = trimToNull(rawPromoCode);
        if (promoCode == null) {
            return null;
        }

        Coupon coupon = couponRepository.findByMerchantId(store.getMerchant().getId()).stream()
                .filter(item -> item.getCode().equalsIgnoreCase(promoCode))
                .filter(item -> item.getStatus() == CouponStatus.ACTIVE)
                .filter(item -> item.getScope() == CouponScope.MERCHANT || (item.getScope() == CouponScope.STORE && item.getStore() != null && item.getStore().getId().equals(store.getId())))
                .findFirst()
                .orElseThrow(() -> businessException(request, INVALID_INPUT_ERROR, "Promo code is invalid"));

        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code is not active yet");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code has expired");
        }
        if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Order does not meet minimum amount for promo code");
        }
        if (coupon.getUsageLimit() != null && couponRedemptionRepository.countByCouponId(coupon.getId()) >= coupon.getUsageLimit()) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code usage limit reached");
        }
        if (coupon.getPerUserLimit() != null) {
            long userUsage = couponRedemptionRepository.findByCouponId(coupon.getId()).stream()
                    .filter(redemption -> redemption.getUser().getId().equals(userId))
                    .count();
            if (userUsage >= coupon.getPerUserLimit()) {
                throw businessException(request, INVALID_INPUT_ERROR, "Promo code per-user limit reached");
            }
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENT
                ? subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    private void processWalletPayment(User customer, BigDecimal amount, FoodOrder order, PlaceOrderRequest request) {
        Wallet wallet = walletRepository.findByUserId(customer.getId())
                .orElseThrow(() -> businessException(request, INVALID_INPUT_ERROR, "Wallet not found. Please top up first."));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Insufficient wallet balance");
        }

        // 1. Deduct balance
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // 2. Create Wallet Transaction for Audit
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount.negate()) // Negative for payment
                .type(WalletTransactionType.PAYMENT)
                .status(WalletTransactionStatus.SUCCESS)
                .description("Payment for order " + order.getCode())
                .referenceId(order.getId().toString())
                .build();
        walletTransactionRepository.save(transaction);
    }

    private PaymentStatus resolvePaymentStatus(PaymentMethod paymentMethod) {
        return PaymentStatus.PENDING;
    }

    private String resolvePaymentProvider(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case CARD -> "CARD";
            case E_WALLET -> "WALLET";
            case BANK_TRANSFER -> "BANK_TRANSFER";
            case COD -> "CASH";
        };
    }

    private PaymentMethod resolvePaymentMethod(String rawValue, BaseRequest request) {
        String value = trimToNull(rawValue);
        if (value == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Payment method is required");
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "cash" -> PaymentMethod.COD;
            case "card" -> PaymentMethod.CARD;
            case "wallet" -> PaymentMethod.E_WALLET;
            default -> throw businessException(request, INVALID_INPUT_ERROR, "Unsupported payment method");
        };
    }

    private ServiceMethod resolveServiceMethod(String rawValue, BaseRequest request) {
        String value = trimToNull(rawValue);
        if (value == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Service method is required");
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "delivery" -> ServiceMethod.DELIVERY;
            case "pickup" -> ServiceMethod.PICKUP;
            default -> throw businessException(request, INVALID_INPUT_ERROR, "Unsupported service method");
        };
    }

    private String resolveCurrency(String currency) {
        String normalized = trimToNull(currency);
        return normalized == null ? "VND" : normalized.toUpperCase(Locale.ROOT);
    }

    private AddressSnapshot toDeliveryAddress(ServiceMethod serviceMethod, PlaceOrderRequest.ShippingAddress shippingAddress) {
        AddressSnapshot address = new AddressSnapshot();
        if (serviceMethod == ServiceMethod.PICKUP || shippingAddress == null) {
            return address;
        }
        address.setLine1(trimToNull(shippingAddress.getFullAddress()));
        address.setLatitude(shippingAddress.getLatitude());
        address.setLongitude(shippingAddress.getLongitude());
        address.setContactName(trimToNull(shippingAddress.getReceiverName()));
        address.setContactPhone(trimToNull(shippingAddress.getReceiverPhone()));
        return address;
    }

    private static final BigDecimal POINT_EXCHANGE_RATE = new BigDecimal("1000");

    private void awardLoyaltyPoints(User customer, List<OrderItem> items) {
        customerMembershipRepository.findByUserId(customer.getId()).ifPresent(membership -> {
            BigDecimal totalBasePoints = items.stream()
                    .map(item -> {
                        // 1000 VND = 1 Point, rounded down
                        BigDecimal pointsPerItem = item.getUnitPrice()
                                .divide(POINT_EXCHANGE_RATE, 0, RoundingMode.FLOOR);
                        return pointsPerItem.multiply(BigDecimal.valueOf(item.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalBasePoints.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal multiplier = membership.getPointMultiplier() != null ? 
                                       membership.getPointMultiplier() : BigDecimal.ONE;
                BigDecimal earnedPoints = totalBasePoints.multiply(multiplier);

                // Update membership points
                membership.setCurrentAvailablePoints(membership.getCurrentAvailablePoints().add(earnedPoints));
                membership.setTotalPoints(membership.getTotalPoints().add(earnedPoints));
                customerMembershipRepository.save(membership);
            }
        });
    }

    private String generateOrderCode() {
        return "ODR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private OrderPayload toPayload(FoodOrder order, List<OrderItem> items, Payment payment) {
        return CustomerOrderMapper.toPayload(order, items, payment);
    }

    private OrderPayload toPayload(FoodOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return CustomerOrderMapper.toPayload(order, items, payment);
    }

    private <T> T requireData(BaseRequest request, T data) {
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }
        return data;
    }

    private User resolveCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isBlank()) {
            return userRepository.findByEmail(currentUser.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        try {
            UUID userId = UUID.fromString(currentUser.subject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        } catch (IllegalArgumentException exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid current user");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validatePagination(BaseRequest request, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_NUMBER);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_SIZE);
        }
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }

    private record ResolvedOrderItem(
            vn.com.orchestration.foodios.entity.catalog.Product product,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
    }
}
