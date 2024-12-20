package com.rental.camp.order.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.model.UserCoupon;
import com.rental.camp.coupon.model.type.CouponType;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.coupon.repository.UserCouponRepository;
import com.rental.camp.order.dto.*;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
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


    public List<CartItem> findCartItems(OrderRequest request) {
        List<Long> requestedIds = request.getCartItemIds();
        List<CartItem> cartItems = cartItemRepository.findAllById(requestedIds);
        Set<Long> foundIds = cartItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toSet());
        List<Long> notFoundIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new EntityNotFoundException("다음 ID의 장바구니 항목을 찾을 수 없습니다: " + notFoundIds);
        }
        return cartItems;
    }

    // PENDING 상태 주문 생성
    @Transactional
    public OrderResponse createOrder(String uuid, OrderRequest requestDTO) {

        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        checkPendingOrderConflicts(userId, requestDTO);
        Order order = orderRepository.save(createInitialOrder(uuid, requestDTO));
        List<CartItem> cartItems = findCartItems(requestDTO);
        Map<Long, RentalItem> rentalItemMap = orderRepository.findRentalItemsByIds(
                cartItems.stream().map(CartItem::getRentalItemId).collect(Collectors.toList())
        );
        long rentalDays = calculateRentalDays(order);

        // 주문 아이템 생성 (가격 계산 없이)
        createOrderItems(order, cartItems, rentalItemMap, rentalDays);

        // 총 아이템 가격 계산
        BigDecimal totalItemPrice = calculateTotalItemPrice(order, cartItems, rentalItemMap);

        // 쿠폰 적용 및 총 금액 업데이트
        processCouponAndUpdateTotal(order, requestDTO.getUserCouponId(), totalItemPrice);

        User user = orderRepository.findUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User를 찾을 수 없습니다: " + userId));
        if (!StringUtils.hasText(user.getAddress()) || !StringUtils.hasText(user.getPhone())) {
            throw new RuntimeException("전화번호, 주소를 마이페이지에서 입력 후 시도해주세요");
        }
        List<OrderItemInfo> orderItems = orderRepository.findOrderItemsWithDetails(order.getId());

        return createOrderResponse(order, user, orderItems, totalItemPrice, rentalDays);
    }

    @Transactional
    private void checkRentalItemStock(OrderRequest request, Long userId) {
        List<CartItem> shortageStockList = orderRepository.checkRentalItemStock(request.getCartItemIds(), userId);
        if (!shortageStockList.isEmpty()) {
            StringBuilder message = new StringBuilder("재고 부족:\n");
            for (CartItem cartItem : shortageStockList) {
                Optional<RentalItem> item = rentalItemRepository.findById(cartItem.getRentalItemId());
                String name = item.map(RentalItem::getName)
                        .orElse("이름 없음");
                Integer stock = item.map(RentalItem::getStock)
                        .orElse(0);
                message.append("아이템명: ").append(name)  // 아이템명
                        .append(", 주문 수량: ").append(cartItem.getQuantity())  // 수량
                        .append(", 재고: ").append(stock)
                        .append("\n")
                        .append("예약 취소 후 다시 주문해 주세요")
                        .append("\n");
            }
            throw new IllegalArgumentException(message.toString());
        }
    }

    @Transactional
    private void findOverStockCartItemsByOrderId(Long userId, Long orderId) {
        List<CartItem> shortageStockList = orderRepository.findOverStockCartItemsByOrderId(orderId, userId);
        if (!shortageStockList.isEmpty()) {
            StringBuilder message = new StringBuilder("재고 부족:\n");
            for (CartItem cartItem : shortageStockList) {
                Optional<RentalItem> item = rentalItemRepository.findById(cartItem.getRentalItemId());
                String name = item.map(RentalItem::getName)
                        .orElse("이름 없음");
                Integer stock = item.map(RentalItem::getStock)
                        .orElse(0);
                message.append("아이템명: ").append(name)  // 아이템명
                        .append(", 주문 수량: ").append(cartItem.getQuantity())  // 수량
                        .append(", 재고: ").append(stock)
                        .append("\n")
                        .append("예약 취소 후 다시 주문해 주세요")
                        .append("\n");
            }
            throw new IllegalArgumentException(message.toString());
        }
    }


    private void checkPendingOrderConflicts(Long userId, OrderRequest requestDTO) {
        // 동일한 장바구니 항목이 PENDING 상태 주문에 사용 중인지 확인
        boolean cartItemConflict = orderRepository.existsByUserIdAndStatusAndCartItemIds(
                userId, OrderStatus.PENDING, requestDTO.getCartItemIds()
        );
        if (cartItemConflict) {
            throw new RuntimeException("예약 상태의 주문에서 이미 사용 중인 장바구니 항목이 있습니다.");
        }

        // 동일한 쿠폰이 PENDING 상태 주문에 사용 중인지 확인
        if (requestDTO.getUserCouponId() != null) {
            boolean couponConflict = orderRepository.existsByUserIdAndStatusAndCouponId(
                    userId, OrderStatus.PENDING, requestDTO.getUserCouponId()
            );
            if (couponConflict) {
                throw new RuntimeException("예약 상태의 주문에서 이미 사용 중인 쿠폰이 있습니다.");
            }
        }
    }

    private OrderResponse createOrderResponse(Order order, User user,
                                              List<OrderItemInfo> orderItems, BigDecimal totalItemPrice, long rentalDays) {

        totalItemPrice = totalItemPrice.setScale(0, RoundingMode.DOWN);


        BigDecimal finalPrice = order.getTotalAmount().setScale(0, RoundingMode.DOWN);


        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);

        BigDecimal finalDiscountAmount = discountAmount.abs().compareTo(BigDecimal.ONE) >= 0 ? discountAmount : null;


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

//            if (rentalItem.getStock() < orderItem.getQuantity()) {
//                throw new RuntimeException(
//                        "재고가 부족한 상품이 있습니다. 상품 ID: " + orderItem.getRentalItemId());
//            }

            rentalItem.setStock(rentalItem.getStock() - orderItem.getQuantity());
            rentalItemRepository.save(rentalItem);
        }
    }

    @Transactional
    public OrderResponse confirmOrder(String uuid, ConfirmRequestData request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Order existingOrder = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        findOverStockCartItemsByOrderId(userId, existingOrder.getId());
        orderRepository.updateOrderStatus(existingOrder.getId(), OrderStatus.COMPLETED);
        if (existingOrder.getCouponId() != null) {
            updateUserCouponStatus(existingOrder);
        }
        List<OrderItemInfo> orderItems = orderRepository.findOrderItemsWithDetails(existingOrder.getId());

        processOrderCompletion(existingOrder, orderItems);
        return createCompletedOrderResponse(existingOrder, orderItems);
    }

    @Transactional
    public OrderResponse completeOrder(String uuid, OrderRequest request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        checkRentalItemStock(request, userId);
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
        if (coupon == null) return BigDecimal.ZERO;

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (coupon.getType() == CouponType.FIXED_AMOUNT_DISCOUNT) {
            discountAmount = coupon.getDiscount().setScale(0, RoundingMode.DOWN);
        } else if (coupon.getType() == CouponType.PERCENTAGE_DISCOUNT) {
            discountAmount = totalAmount.multiply(coupon.getDiscount())
                    .divide(BigDecimal.valueOf(100), RoundingMode.DOWN)
                    .setScale(0, RoundingMode.DOWN);
        }

        return discountAmount;
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

        }

        return totalItemPrice;
    }

    @Transactional
    private void processCouponAndUpdateTotal(Order order, Long couponId, BigDecimal totalItemPrice) {
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (couponId != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("Coupon을 찾을 수 없습니다: " + couponId));
            discountAmount = applyCouponDiscount(totalItemPrice, coupon);
        }

        BigDecimal finalPrice = totalItemPrice.subtract(discountAmount)
                .max(BigDecimal.ZERO)
                .setScale(0, RoundingMode.DOWN);
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

    private BigDecimal calculateTotalItemPrice(Order order, List<CartItem> cartItems, Map<Long, RentalItem> rentalItemMap) {
        if (cartItems == null || cartItems.isEmpty()) return BigDecimal.ZERO;

        long rentalDays = calculateRentalDays(order);

        BigDecimal totalItemPrice = cartItems.stream()
                .map(cartItem -> {
                    RentalItem rentalItem = rentalItemMap.get(cartItem.getRentalItemId());
                    BigDecimal price = rentalItem != null ? rentalItem.getPrice() : BigDecimal.ZERO;
                    int quantity = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(rentalDays))
                .setScale(0, RoundingMode.DOWN);

        return totalItemPrice;
    }


    private BigDecimal calculateDiscountAmount(BigDecimal totalItemPrice, BigDecimal finalPrice) {
        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);
        return discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null;
    }

    private long calculateRentalDays(Order order) {
        LocalDate rentalDate = order.getRentalDate().toLocalDate();
        LocalDate returnDate = order.getReturnDate().toLocalDate();

        // 오늘 날짜 가져오기
        LocalDate today = LocalDate.now();

        // 날짜 차이 계산 (반납일 포함)
        if (!rentalDate.isAfter(returnDate)) {
            long daysBetween = ChronoUnit.DAYS.between(rentalDate, returnDate) + 1;
            return daysBetween;
        } else {
            throw new IllegalArgumentException("반납일은 대여일보다 이후여야 합니다.");
        }
    }

    private String formatCreatedAt(LocalDateTime createdAt) {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public List<PendingOrderResponse> findPendingOrder(String uuid) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        List<Order> orders = orderRepository.findPendingOrderByUser(userId);
        if (orders.isEmpty()) {
            throw new RuntimeException("예약 중인 주문이 없습니다.");
        }
        return orders.stream().map(order -> new PendingOrderResponse(
                order.getUserId(),
                order.getId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        )).collect(Collectors.toList());
    }


    @Transactional
    public void deletePending(String uuid, Long orderId) {
        try {
            Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();

            Order order = orderRepository.findPendingOrderByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("예약 중인 주문이 없습니다."));

            if (order.getUserId().equals(userId)) {
                orderRepository.deleteOrderItemsByOrderId(orderId);
                orderRepository.delete(order);
            } else {
                throw new IllegalStateException("해당 주문은 이 사용자에게 속하지 않습니다.");
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new RuntimeException(e.getMessage(), e);

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("예약 중인 주문을 찾을 수 없습니다.", e);

        } catch (Exception e) {
            throw new RuntimeException("오류가 발생했습니다. 다시 시도해주세요.", e);
        }
    }

    // PENDING 상태의 주문을 찾아 totalAmount 반환
    public BigDecimal getPendingOrderTotalAmount(String uuid) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        // PENDING 상태의 주문을 찾기
        Optional<Order> orderOptional = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.PENDING);

        // 주문이 존재하면 totalAmount를 반환
        return orderOptional.map(Order::getTotalAmount)
                .orElse(BigDecimal.ZERO); // 주문이 없으면 0 반환
    }

    public PaymentInfo getPaymentInfo(String uuid) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        // 주문 정보 조회
        Order order = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾지 못했습니다"));

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾지 못했습니다"));

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setOrderName("캠퍼파이 결제");
        paymentInfo.setSuccessUrl("");
        paymentInfo.setFailUrl("");
        paymentInfo.setCustomerEmail(user.getEmail());
        paymentInfo.setCustomerName(user.getUsername());
        paymentInfo.setCustomerMobilePhone(user.getPhone());
        return paymentInfo;
    }


}




