package com.rental.camp.order.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.model.UserCoupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.coupon.repository.UserCouponRepository;
import com.rental.camp.order.dto.OrderItemInfo;
import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.dto.OrderResponse;
import com.rental.camp.order.dto.PendingOrderResponse;
import com.rental.camp.order.model.CartItem;
import com.rental.camp.order.model.Order;
import com.rental.camp.order.model.OrderItem;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.order.repository.CartItemRepository;
import com.rental.camp.order.repository.OrderItemRepository;
import com.rental.camp.order.repository.OrderRepository;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.repository.RentalItemRepository;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final JPAQueryFactory queryFactory;
    private final UserCouponRepository userCouponRepository;
    private final RentalItemRepository rentalItemRepository;
    private final UserRepository userRepository;


    // PENDING 상태 주문 생성
    @Transactional
    public OrderResponse createOrder(String uuid, OrderRequest requestDTO) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order order = orderRepository.save(createInitialOrder(uuid, requestDTO));
        List<CartItem> cartItems = cartItemRepository.findAllById(requestDTO.getCartItemIds());

        Map<Long, RentalItem> rentalItemMap = orderRepository.findRentalItemsByIds(
                cartItems.stream().map(CartItem::getRentalItemId).collect(Collectors.toList())
        );
        long rentalDays = calculateRentalDays(order);

        BigDecimal totalItemPrice = createOrderItems(order, cartItems, rentalItemMap, rentalDays)
                .setScale(0, RoundingMode.DOWN);
        processCouponAndUpdateTotal(order, requestDTO.getUserCouponId(), totalItemPrice);

        User user = orderRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User를 찾을 수 없습니다: " + userId));

        List<OrderItemInfo> orderItems = orderRepository.findOrderItemsWithDetails(order.getId());

        return createOrderResponse(order, user, orderItems, totalItemPrice, rentalDays);
    }

    private OrderResponse createOrderResponse(Order order, User user,
                                              List<OrderItemInfo> orderItems, BigDecimal totalItemPrice, long rentalDays) {

        totalItemPrice = totalItemPrice.setScale(0, RoundingMode.DOWN);


        BigDecimal finalPrice = order.getTotalAmount().setScale(0, RoundingMode.DOWN);


        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);


        discountAmount = discountAmount.setScale(0, RoundingMode.DOWN);


        BigDecimal finalDiscountAmount = discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCreatedAt = order.getCreatedAt().format(formatter);

        return new OrderResponse(
                order.getUserId(),
                "예약이 성공적으로 생성되었습니다.",
                order.getId(),
                user.getUsername(),
                user.getAddress(),
                user.getPhone(),
                orderItems,
                rentalDays,
                totalItemPrice,
                finalDiscountAmount,
                finalPrice,
                formattedCreatedAt
        );
    }


    @Transactional
    private void updateUserCouponStatus(Order order) {
        UserCoupon userCoupon = userCouponRepository
                .findByCouponIdAndUserId(order.getCouponId(), order.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "사용자의 쿠폰을 찾을 수 없습니다. CouponId: " + order.getCouponId() +
                                ", UserId: " + order.getUserId()));

        if (!userCoupon.getIsUsed()) {
            userCoupon.setIsUsed(true);
            userCouponRepository.save(userCoupon);
        }
    }

    @Transactional
    private void processOrderCompletion(Order existingOrder, List<OrderItemInfo> orderItems) {
        // 장바구니 항목 삭제
        List<Long> orderRentalItemIds = orderItems.stream()
                .map(OrderItemInfo::getRentalItemId)
                .collect(Collectors.toList());

        cartItemRepository.deleteAllByUserIdAndRentalItemIdIn(
                existingOrder.getUserId(),
                orderRentalItemIds
        );

        // 재고 감소
        for (OrderItemInfo orderItem : orderItems) {
            RentalItem rentalItem = rentalItemRepository
                    .findById(orderItem.getRentalItemId())
                    .orElseThrow(() -> new RuntimeException(
                            "RentalItem을 찾을 수 없습니다. ID: " + orderItem.getRentalItemId()));

            if (rentalItem.getStock() < orderItem.getQuantity()) {
                throw new RuntimeException(
                        "재고가 부족한 상품이 있습니다. 상품 ID: " + orderItem.getRentalItemId());
            }

            rentalItem.setStock(rentalItem.getStock() - orderItem.getQuantity());
            rentalItemRepository.save(rentalItem);
        }
    }

    @Transactional
    public OrderResponse completeOrder(String uuid, OrderRequest request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order existingOrder = orderRepository.findPendingOrderByUserAndItem(uuid, request)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        orderRepository.updateOrderStatus(existingOrder.getId(), OrderStatus.COMPLETED);

        if (existingOrder.getCouponId() != null) {
            updateUserCouponStatus(existingOrder);
        }

        List<OrderItemInfo> orderItems = orderRepository.findOrderItemsWithDetails(existingOrder.getId());

        processOrderCompletion(existingOrder, orderItems);

        return createCompletedOrderResponse(existingOrder, orderItems);
    }

    private OrderResponse createCompletedOrderResponse(Order order, List<OrderItemInfo> orderItems) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User를 찾을 수 없습니다: " + order.getUserId()));

        BigDecimal totalItemPrice = orderItems.stream()
                .map(OrderItemInfo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long rentalDays = calculateRentalDays(order);
        OrderResponse completeResponse = createOrderResponse(order, user, orderItems, totalItemPrice, rentalDays);
        completeResponse.setMessage("주문이 성공적으로 완료되었습니다.");
        return completeResponse;
    }


    private BigDecimal applyCouponDiscount(BigDecimal totalAmount, Coupon coupon) {
        return switch (coupon.getType()) {
            case PERCENTAGE_DISCOUNT -> totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscount()));
            case FIXED_AMOUNT_DISCOUNT -> totalAmount.subtract(coupon.getDiscount()).max(BigDecimal.ZERO);
            //  default -> totalAmount;
        };
    }

    private OrderResponse createOrderDetailsResponse(Order order, List<OrderItemInfo> orderItems) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User를 찾을 수 없습니다: " + order.getUserId()));

        BigDecimal totalItemPrice = orderItems.stream()
                .map(OrderItemInfo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = order.getTotalAmount();
        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);
        BigDecimal finalDiscountAmount = discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null;

        long rentalDays = calculateRentalDays(order);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCreatedAt = order.getCreatedAt().format(formatter);
        String updatedAt = String.valueOf(order.getUpdatedAt());

        return new OrderResponse(
                order.getUserId(),
                String.format("주문 조회가 완료되었습니다. 주문 상태: %s", order.getOrderStatus()),
                order.getId(),
                user.getUsername(),
                user.getAddress(),
                user.getPhone(),
                orderItems,
                rentalDays,
                totalItemPrice,
                finalDiscountAmount,
                finalPrice,
                formattedCreatedAt,
                updatedAt
        );
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem, RentalItem item, long rentalDays) {
        BigDecimal pricePerDay = item.getPrice();
        BigDecimal subtotal = pricePerDay
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                .multiply(BigDecimal.valueOf(rentalDays));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setRentalItemId(cartItem.getRentalItemId());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(pricePerDay);
        orderItem.setSubtotal(subtotal);

        return orderItem;
    }


    public OrderResponse getOrderDetails(String uuid, Long orderId) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order order = orderRepository.findOrderByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        List<OrderItemInfo> orderItems = orderRepository.findOrderItemsWithDetails(orderId);

        if (orderItems.isEmpty()) {
            throw new RuntimeException("주문 아이템을 찾을 수 없습니다.");
        }

        return createOrderDetailsResponse(order, orderItems);
    }

    @Transactional
    public OrderResponse cancelOrder(String uuid, Long orderId) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order order = orderRepository.findCancellableOrder(orderId, userId)
                .orElseThrow(() -> new RuntimeException("취소할 수 없는 주문입니다."));

        orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);

        restoreUserCoupon(order);

        List<OrderItemInfo> orderItems = orderRepository.findOrderItemsWithDetails(orderId);
        restoreRentalItemsStock(orderItems);

        return createOrderCancellationResponse(order, orderItems);
    }


    @Transactional
    private BigDecimal createOrderItems(Order order, List<CartItem> cartItems,
                                        Map<Long, RentalItem> rentalItemMap, long rentalDays) {
        BigDecimal totalItemPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            RentalItem item = rentalItemMap.get(cartItem.getRentalItemId());
            if (item == null) {
                throw new RuntimeException("RentalItem을 찾을 수 없습니다: " + cartItem.getRentalItemId());
            }

            OrderItem orderItem = createOrderItem(order, cartItem, item, rentalDays);
            orderItemRepository.save(orderItem);
            totalItemPrice = totalItemPrice.add(orderItem.getSubtotal()).setScale(0, RoundingMode.DOWN);
            ;
        }

        return totalItemPrice;
    }

    @Transactional
    private void processCouponAndUpdateTotal(Order order, Long couponId, BigDecimal totalItemPrice) {
        BigDecimal finalPrice = totalItemPrice;

        if (couponId != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("Coupon을 찾을 수 없습니다: " + couponId));
            finalPrice = applyCouponDiscount(totalItemPrice, coupon);
        }

        order.setTotalAmount(finalPrice);
        orderRepository.save(order);
    }

    private Order createInitialOrder(String uuid, OrderRequest requestDTO) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setRentalDate(requestDTO.getRentalDate());
        order.setReturnDate(requestDTO.getReturnDate());
        order.setCouponId(requestDTO.getUserCouponId());
        return order;
    }

    @Transactional
    private void restoreUserCoupon(Order order) {
        if (order.getCouponId() != null) {
            UserCoupon userCoupon = userCouponRepository.findByCouponIdAndUserId(order.getCouponId(), order.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자의 쿠폰을 찾을 수 없습니다. CouponId: " + order.getCouponId() + ", UserId: " + order.getUserId()));

            userCoupon.setIsUsed(false);
            userCouponRepository.save(userCoupon);
        }
    }

    @Transactional
    private void restoreRentalItemsStock(List<OrderItemInfo> orderItems) {
        // rentalItemId를 키로 하고, 수량을 값으로 하는 맵 생성
        Map<Long, Integer> rentalItemQuantities = new HashMap<>();
        for (OrderItemInfo orderItem : orderItems) {
            rentalItemQuantities.merge(orderItem.getRentalItemId(), orderItem.getQuantity(), Integer::sum);
        }

        // 그룹화된 데이터를 기반으로 재고 복원
        for (Map.Entry<Long, Integer> entry : rentalItemQuantities.entrySet()) {
            Long rentalItemId = entry.getKey();
            Integer totalQuantity = entry.getValue();
            RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                    .orElseThrow(() -> new RuntimeException("RentalItem을 찾을 수 없습니다. ID: " + rentalItemId));

            rentalItem.setStock(rentalItem.getStock() + totalQuantity);
            rentalItemRepository.save(rentalItem);
        }
    }

    private OrderResponse createOrderCancellationResponse(Order order, List<OrderItemInfo> orderItems) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User를 찾을 수 없습니다: " + order.getUserId()));

        BigDecimal totalItemPrice = calculateTotalItemPrice(orderItems);
        BigDecimal finalDiscountAmount = calculateDiscountAmount(totalItemPrice, order.getTotalAmount());
        long rentalDays = calculateRentalDays(order);
        String formattedCreatedAt = formatCreatedAt(order.getCreatedAt());

        return new OrderResponse(
                order.getUserId(),
                "주문이 취소되었습니다.",
                order.getId(),
                user.getUsername(),
                user.getAddress(),
                user.getPhone(),
                orderItems,
                rentalDays,
                totalItemPrice,
                finalDiscountAmount,
                order.getTotalAmount(),
                formattedCreatedAt
        );
    }

    private BigDecimal calculateTotalItemPrice(List<OrderItemInfo> orderItems) {
        return orderItems.stream()
                .map(OrderItemInfo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscountAmount(BigDecimal totalItemPrice, BigDecimal finalPrice) {
        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);
        return discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null;
    }

    private long calculateRentalDays(Order order) {
        long days = ChronoUnit.DAYS.between(order.getRentalDate().toLocalDate(), order.getReturnDate().toLocalDate());
        return days <= 0 ? 1 : days;
    }

    private String formatCreatedAt(LocalDateTime createdAt) {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public PendingOrderResponse findPendingOrder(String uuid) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order order = orderRepository.findPendingOrderByUser(userId)
                .orElseThrow(() -> new RuntimeException("예약 중인 주문이 없습니다."));
        return new PendingOrderResponse(
                order.getUserId(),
                order.getId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );


    }
}




