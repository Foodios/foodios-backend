package vn.com.orchestration.foodios.service.cart.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.cart.AddCartItemRequest;
import vn.com.orchestration.foodios.dto.cart.AddCartItemResponse;
import vn.com.orchestration.foodios.dto.cart.CartItemPayload;
import vn.com.orchestration.foodios.dto.cart.CartPayload;
import vn.com.orchestration.foodios.dto.cart.GetCartResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.cart.Cart;
import vn.com.orchestration.foodios.entity.cart.CartItem;
import vn.com.orchestration.foodios.entity.cart.CartStatus;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.CartItemRepository;
import vn.com.orchestration.foodios.repository.CartRepository;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.cart.CustomerCartService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class CustomerCartServiceImpl implements CustomerCartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional
    public AddCartItemResponse addItem(AddCartItemRequest request) {
        AddCartItemRequest.AddCartItemRequestData data = requireData(request, request.getData());
        User customer = resolveCurrentUser(request);

        Store store = storeRepository.findById(data.getStoreId())
                .filter(item -> item.getStatus() == StoreStatus.ACTIVE)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));

        Product product = productRepository.findById(data.getProductId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Product not found"));

        if (!product.getStore().getId().equals(store.getId())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Product does not belong to store");
        }
        if (product.getStatus() != ProductStatus.ACTIVE || !product.isAvailable()) {
            throw businessException(request, INVALID_INPUT_ERROR, "Product is not available");
        }

        Cart cart = cartRepository.findByUserIdAndStoreId(customer.getId(), store.getId())
                .orElseGet(() -> createCart(customer, store));
        if (cart.getStatus() != CartStatus.ACTIVE) {
            cart.setStatus(CartStatus.ACTIVE);
            cart = cartRepository.save(cart);
        }

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);
        if (cartItem == null) {
            cartItem = createCartItem(cart, product);
        }

        int updatedQuantity = cartItem.getQuantity() + data.getQuantity();
        cartItem.setQuantity(updatedQuantity);
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(updatedQuantity)));
        cartItemRepository.saveAndFlush(cartItem);

        return AddCartItemResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toCartPayload(cart))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCartResponse getCart(BaseRequest request, UUID storeId) {
        User customer = resolveCurrentUser(request);
        Store store = storeRepository.findById(storeId)
                .filter(item -> item.getStatus() == StoreStatus.ACTIVE)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));

        CartPayload payload = cartRepository.findByUserIdAndStoreId(customer.getId(), store.getId())
                .map(this::toCartPayload)
                .orElseGet(() -> emptyCartPayload(customer.getId(), store.getId()));

        return GetCartResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(payload)
                .build();
    }

    private Cart createCart(User user, Store store) {
        return cartRepository.save(Cart.builder()
                .user(user)
                .store(store)
                .status(CartStatus.ACTIVE)
                .build());
    }

    private CartItem createCartItem(Cart cart, Product product) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(0)
                .unitPrice(product.getPrice())
                .totalPrice(BigDecimal.ZERO)
                .build();
    }

    private CartPayload toCartPayload(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId()).stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        return toCartPayload(cart, items);
    }

    private CartPayload toCartPayload(Cart cart, List<CartItem> items) {
        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal subtotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartPayload.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .storeId(cart.getStore().getId())
                .status(cart.getStatus())
                .totalQuantity(totalQuantity)
                .subtotal(subtotal)
                .items(items.stream().map(this::toItemPayload).toList())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartPayload emptyCartPayload(UUID userId, UUID storeId) {
        return CartPayload.builder()
                .id(null)
                .userId(userId)
                .storeId(storeId)
                .status(CartStatus.ACTIVE)
                .totalQuantity(0)
                .subtotal(BigDecimal.ZERO)
                .items(List.of())
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    private CartItemPayload toItemPayload(CartItem item) {
        return CartItemPayload.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSlug(item.getProduct().getSlug())
                .imageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
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

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
