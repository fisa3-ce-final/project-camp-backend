package com.rental.camp.order.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.model.UserCoupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.coupon.repository.UserCouponRepository;
import com.rental.camp.order.dto.OrderConflict;
import com.rental.camp.order.dto.OrderItemInfo;
import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.dto.OrderResponse;
import com.rental.camp.order.model.*;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.order.repository.CartItemRepository;
import com.rental.camp.order.repository.OrderItemRepository;
import com.rental.camp.order.repository.OrderRepository;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.repository.RentalItemRepository;
import com.rental.camp.user.model.QUser;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final JPAQueryFactory queryFactory;
    private final UserCouponRepository userCouponRepository;
    private final RentalItemRepository rentalItemRepository;
    private final UserRepository userRepository;


    @Transactional
    public OrderResponse createOrder(OrderRequest requestDTO) {
        //TO-DO


        // 대여 기간 중복 여부 확인
//        List<OrderConflict> conflicts = checkForConflicts(requestDTO);
//        if (!conflicts.isEmpty()) {
//            // 중복된 아이템 정보를 포함한 응답 반환
//            return new OrderResponse("예약 불가 - 중복된 대여 기간이 있습니다.", requestDTO.getUserId(), conflicts);
//        }

        // 주문 생성
        Order order = new Order();
        order.setUserId(requestDTO.getUserId());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setRentalDate(requestDTO.getRentalDate());
        order.setReturnDate(requestDTO.getReturnDate());
        order.setCouponId(requestDTO.getUserCouponId()); // 쿠폰 ID 설정 (선택적)

        // 주문 저장
        orderRepository.save(order);

        // 주문 아이템 생성 및 저장
        List<CartItem> cartItems = cartItemRepository.findAllById(requestDTO.getCartItemIds());

        // RentalItem 정보를 QueryDSL로 조회하여 가격 정보 가져오기
        List<Long> rentalItemIds = cartItems.stream()
                .map(CartItem::getRentalItemId)
                .collect(Collectors.toList());

        QRentalItem qRentalItem = QRentalItem.rentalItem;

        List<RentalItem> rentalItems = queryFactory
                .selectFrom(qRentalItem)
                .where(qRentalItem.id.in(rentalItemIds))
                .fetch();

        Map<Long, RentalItem> rentalItemMap = rentalItems.stream()
                .collect(Collectors.toMap(RentalItem::getId, Function.identity()));

        BigDecimal totalItemPrice = BigDecimal.ZERO;

        long rentalDays = ChronoUnit.DAYS.between(order.getRentalDate().toLocalDate(), order.getReturnDate().toLocalDate());
        if (rentalDays <= 0) {
            rentalDays = 1;
        }

        // 주문 아이템 생성 및 총 금액 계산
        for (CartItem cartItem : cartItems) {
            RentalItem item = rentalItemMap.get(cartItem.getRentalItemId());
            if (item == null) {
                throw new RuntimeException("RentalItem을 찾을 수 없습니다: " + cartItem.getRentalItemId());
            }

            BigDecimal pricePerDay = item.getPrice();
            BigDecimal subtotal = pricePerDay.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    .multiply(BigDecimal.valueOf(rentalDays));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setRentalItemId(cartItem.getRentalItemId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(pricePerDay);
            orderItem.setSubtotal(subtotal);
            orderItemRepository.save(orderItem);

            totalItemPrice = totalItemPrice.add(subtotal);
        }

        // 쿠폰 적용 후 최종 금액 계산
        BigDecimal finalPrice = totalItemPrice;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (requestDTO.getUserCouponId() != null) {
            Coupon coupon = couponRepository.findById(requestDTO.getUserCouponId())
                    .orElseThrow(() -> new RuntimeException("Coupon을 찾을 수 없습니다: " + requestDTO.getUserCouponId()));
            BigDecimal discountedPrice = applyCouponDiscount(totalItemPrice, coupon);
            discountAmount = totalItemPrice.subtract(discountedPrice);
            finalPrice = discountedPrice;
        }

        // 주문의 총 금액 업데이트
        order.setTotalAmount(finalPrice);
        orderRepository.save(order);

        // User 정보를 QueryDSL로 조회하여 username, address, phone 가져오기
        QUser qUser = QUser.user;

        User userEntity = queryFactory
                .selectFrom(qUser)
                .where(qUser.id.eq(requestDTO.getUserId()))
                .fetchOne();

        if (userEntity == null) {
            throw new RuntimeException("User를 찾을 수 없습니다: " + requestDTO.getUserId());
        }

        // 주문 아이템 정보 조회
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QRentalItem qRentalItemAlias = QRentalItem.rentalItem;

        List<OrderItemInfo> orderItems = queryFactory
                .select(Projections.constructor(OrderItemInfo.class,
                        qOrderItem.rentalItemId,                  // rentalItemId 추가
                        qRentalItemAlias.name.as("itemName"),
                        qOrderItem.quantity,
                        qOrderItem.price,
                        qOrderItem.subtotal))
                .from(qOrderItem)
                .join(qRentalItemAlias).on(qOrderItem.rentalItemId.eq(qRentalItemAlias.id))
                .where(qOrderItem.orderId.eq(order.getId()))
                .fetch();

        // 주문 생성 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCreatedAt = order.getCreatedAt().format(formatter);

        // 응답 객체 생성 및 반환
        return new OrderResponse(
                requestDTO.getUserId(),
                "예약이 성공적으로 생성되었습니다.",
                order.getId(),
                userEntity.getUsername(),
                userEntity.getAddress(),
                userEntity.getPhone(),
                orderItems,
                rentalDays,
                totalItemPrice,
                discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null,
                finalPrice,
                formattedCreatedAt
        );
    }

    @Transactional
    public OrderResponse completeOrder(OrderRequest request) {
        QOrder qOrder = QOrder.order;
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QRentalItem qRentalItem = QRentalItem.rentalItem;
        QUser qUser = QUser.user;


        // 1. 주문 찾기
        Long cartItemId = request.getCartItemIds().get(0);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found: " + cartItemId));

        // 2. 해당 주문 찾기 (PENDING 상태인 주문 중 해당 사용자와 rentalItemId를 가진 주문)
        Order existingOrder = queryFactory
                .selectFrom(qOrder)
                .where(
                        qOrder.userId.eq(request.getUserId())
                                .and(qOrder.orderStatus.eq(OrderStatus.PENDING))
                                .and(
                                        JPAExpressions
                                                .selectOne()
                                                .from(qOrderItem)
                                                .where(
                                                        qOrderItem.orderId.eq(qOrder.id)
                                                                .and(qOrderItem.rentalItemId.eq(cartItem.getRentalItemId()))
                                                )
                                                .exists()
                                )
                )
                .fetchOne();

        if (existingOrder == null) {
            throw new RuntimeException("주문을 찾을 수 없습니다.");
        }

        // 3. 주문 상태를 COMPLETED로 업데이트
        long updatedCount = queryFactory
                .update(qOrder)
                .set(qOrder.orderStatus, OrderStatus.COMPLETED)
                .where(qOrder.id.eq(existingOrder.getId()))
                .execute();

        //  업데이트 결과 확인
        if (updatedCount == 0) {
            throw new RuntimeException("주문 상태 업데이트 실패: 해당하는 주문을 찾을 수 없습니다.");
        }
        // 4. 쿠폰 사용 여부 업데이트
        if (existingOrder.getCouponId() != null) {
            Long couponId = existingOrder.getCouponId();
            Long userId = existingOrder.getUserId();
            UserCoupon userCoupon = userCouponRepository.findByCouponIdAndUserId(couponId, userId)
                    .orElseThrow(() -> new RuntimeException("사용자의 쿠폰을 찾을 수 없습니다. CouponId: " + couponId + ", UserId: " + userId));

            if (!userCoupon.getIsUsed()) { // 이미 사용된 쿠폰인지 확인
                userCoupon.setIsUsed(true);
                userCouponRepository.save(userCoupon);
            }
        }


        // 5. 주문 아이템 정보 조회 및 totalItemPrice 계산
        List<OrderItemInfo> orderItems = queryFactory
                .select(Projections.constructor(OrderItemInfo.class,
                        qOrderItem.rentalItemId,                  // rentalItemId 추가
                        qRentalItem.name.as("itemName"),
                        qOrderItem.quantity,
                        qOrderItem.price,
                        qOrderItem.subtotal))
                .from(qOrderItem)
                .join(qRentalItem).on(qOrderItem.rentalItemId.eq(qRentalItem.id))
                .where(qOrderItem.orderId.eq(existingOrder.getId()))
                .fetch();

        // totalItemPrice 계산
        BigDecimal totalItemPrice = orderItems.stream()
                .map(OrderItemInfo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // finalPrice는 이미 Order의 totalAmount에 저장되어 있음
        BigDecimal finalPrice = existingOrder.getTotalAmount();

        // discountAmount 계산 (finalPrice < totalItemPrice 인 경우)
        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);

        // rentalDays 계산
        long rentalDays = ChronoUnit.DAYS.between(existingOrder.getRentalDate().toLocalDate(), existingOrder.getReturnDate().toLocalDate());
        if (rentalDays <= 0) {
            rentalDays = 1;
        }

        // User 정보 조회
        User userEntity = queryFactory
                .selectFrom(qUser)
                .where(qUser.id.eq(request.getUserId()))
                .fetchOne();

        if (userEntity == null) {
            throw new RuntimeException("User를 찾을 수 없습니다: " + request.getUserId());
        }

        // 할인 금액이 없는 경우 null로 설정
        BigDecimal finalDiscountAmount = discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null;

        // 주문 생성 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCreatedAt = existingOrder.getCreatedAt().format(formatter);

        // 6. 장바구니 삭제 로직 추가
        // 주문에 사용된 모든 OrderItems의 rentalItemId를 기반으로 장바구니 항목을 삭제합니다.
        List<Long> orderRentalItemIds = queryFactory
                .select(qOrderItem.rentalItemId)
                .from(qOrderItem)
                .where(qOrderItem.orderId.eq(existingOrder.getId()))
                .fetch();

        // 사용자 ID와 rentalItemIds를 기반으로 장바구니 항목 삭제

        cartItemRepository.deleteAllByUserIdAndRentalItemIdIn(existingOrder.getUserId(), orderRentalItemIds);

        // 7. RentalItem의 stock 감소 로직 추가

        for (OrderItemInfo orderItem : orderItems) {

            Long rentalItemId = orderItem.getRentalItemId();
            Integer quantity = orderItem.getQuantity();

            RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                    .orElseThrow(() -> new RuntimeException("RentalItem을 찾을 수 없습니다. ID: " + rentalItemId));

            if (rentalItem.getStock() < quantity) {
                throw new RuntimeException("재고가 부족한 상품이 있습니다. 상품 ID: " + rentalItemId);
            }

            rentalItem.setStock(rentalItem.getStock() - quantity);
            rentalItemRepository.save(rentalItem);
        }

        // 응답 객체 생성 및 반환
        return new OrderResponse(
                request.getUserId(),
                "주문이 완료되었습니다.",
                existingOrder.getId(),
                userEntity.getUsername(),
                userEntity.getAddress(),
                userEntity.getPhone(),
                orderItems,
                rentalDays,
                totalItemPrice,
                finalDiscountAmount,
                finalPrice,
                formattedCreatedAt
        );

    }

    private BigDecimal applyCouponDiscount(BigDecimal totalAmount, Coupon coupon) {
        return switch (coupon.getType()) {
            case PERCENTAGE_DISCOUNT -> totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscount()));
            case FIXED_AMOUNT_DISCOUNT -> totalAmount.subtract(coupon.getDiscount()).max(BigDecimal.ZERO);
            //  default -> totalAmount;
        };
    }

    @Transactional
    private List<OrderConflict> checkForConflicts(OrderRequest requestDTO) {
        // 1. 요청한 CartItem으로부터 rentalItemId 목록 조회
        List<com.rental.camp.order.dto.CartItem> cartItems = cartItemRepository.findAllByIdAndUserId(requestDTO.getCartItemIds(), requestDTO.getUserId());
        List<Long> requestedRentalItemIds = cartItems.stream()
                .map(com.rental.camp.order.dto.CartItem::getRentalItemId)
                .collect(Collectors.toList());

        // 2. 대여 기간이 겹치는 주문의 ID 목록 조회
        QOrder qOrder = QOrder.order;

        List<Long> overlappingOrderIds = queryFactory
                .select(qOrder.id)
                .from(qOrder)
                .where(
                        qOrder.orderStatus.ne(OrderStatus.CANCELLED)
                                .and(qOrder.rentalDate.lt(requestDTO.getReturnDate()))
                                .and(qOrder.returnDate.gt(requestDTO.getRentalDate()))
                )
                .fetch();

        List<OrderConflict> conflicts = new ArrayList<>();

        if (!overlappingOrderIds.isEmpty()) {
            // 3. 겹치는 주문의 OrderItem 중에서 요청한 rentalItemId와 겹치는 것 조회
            QOrderItem qOrderItem = QOrderItem.orderItem;
            QRentalItem qRentalItem = QRentalItem.rentalItem;

            List<OrderConflict> dateConflicts = queryFactory
                    .select(Projections.constructor(OrderConflict.class,
                            qRentalItem.name,
                            qOrder.returnDate))
                    .from(qOrderItem)
                    .join(qRentalItem).on(qOrderItem.rentalItemId.eq(qRentalItem.id))
                    .join(qOrder).on(qOrderItem.orderId.eq(qOrder.id))
                    .where(
                            qOrderItem.rentalItemId.in(requestedRentalItemIds)
                                    .and(qOrderItem.orderId.in(overlappingOrderIds))
                    )
                    .fetch();

            conflicts.addAll(dateConflicts);
        }

        // 4. 재고 충돌 확인
        // 요청된 rentalItemId에 해당하는 RentalItem 조회 (재고 정보 포함)
        List<RentalItem> rentalItems = rentalItemRepository.findAllById(requestedRentalItemIds);

        // rentalItemId를 키로 하는 RentalItem 맵 생성
        Map<Long, RentalItem> rentalItemMap = rentalItems.stream()
                .collect(Collectors.toMap(RentalItem::getId, Function.identity()));

        for (com.rental.camp.order.dto.CartItem cartItem : cartItems) {
            RentalItem rentalItem = rentalItemMap.get(cartItem.getRentalItemId());
            if (rentalItem == null) {
                conflicts.add(new OrderConflict("알 수 없는 아이템", "rentalItemId: " + cartItem.getRentalItemId() + "을(를) 찾을 수 없습니다."));
                continue;
            }

            // 현재 재고에서 요청된 수량을 뺀 값 계산
            int remainingStock = rentalItem.getStock() - cartItem.getQuantity();
            if (remainingStock < 0) {
                conflicts.add(new OrderConflict(
                        rentalItem.getName(),
                        "재고 부족: 현재 재고는 " + rentalItem.getStock() + "개이며, 요청된 수량은 " + cartItem.getQuantity() + "개입니다."
                ));
            }
        }

        return conflicts;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId, Long userId) {
        QOrder qOrder = QOrder.order;
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QRentalItem qRentalItem = QRentalItem.rentalItem;
        QUser qUser = QUser.user;

        // 1. 주문 조회
        Order order = queryFactory
                .selectFrom(qOrder)
                .where(
                        qOrder.id.eq(orderId)
                                .and(qOrder.userId.eq(userId))
                )
                .fetchOne();

        if (order == null) {
            throw new RuntimeException("주문을 찾을 수 없습니다.");
        }

        // 2. 주문 아이템 정보 조회 및 totalItemPrice 계산
        List<OrderItemInfo> orderItems = queryFactory
                .select(Projections.constructor(OrderItemInfo.class,
                        qOrderItem.rentalItemId,
                        qRentalItem.name.as("itemName"),
                        qOrderItem.quantity,
                        qOrderItem.price,
                        qOrderItem.subtotal))
                .from(qOrderItem)
                .join(qRentalItem).on(qOrderItem.rentalItemId.eq(qRentalItem.id))
                .where(qOrderItem.orderId.eq(orderId))
                .fetch();

        if (orderItems.isEmpty()) {
            throw new RuntimeException("주문 아이템을 찾을 수 없습니다.");
        }

        // 3. totalItemPrice 계산
        BigDecimal totalItemPrice = orderItems.stream()
                .map(OrderItemInfo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // finalPrice는 order의 totalAmount
        BigDecimal finalPrice = order.getTotalAmount();

        // 4. discountAmount 계산
        BigDecimal discountAmount = totalItemPrice.subtract(finalPrice);
        BigDecimal finalDiscountAmount = discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null;

        // 5. rentalDays 계산
        long rentalDays = ChronoUnit.DAYS.between(order.getRentalDate().toLocalDate(), order.getReturnDate().toLocalDate());
        if (rentalDays <= 0) {
            rentalDays = 1;
        }

        // 6. User 정보 조회
        User user = queryFactory
                .selectFrom(qUser)
                .where(qUser.id.eq(userId))
                .fetchOne();

        if (user == null) {
            throw new RuntimeException("User를 찾을 수 없습니다: " + userId);
        }

        // 7. 주문 생성 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCreatedAt = order.getCreatedAt().format(formatter);
        String updatedAt = String.valueOf(order.getUpdatedAt());
        // 8. 응답 객체 생성 및 반환
        return new OrderResponse(
                userId,
                String.format("주문 조회가 완료되었습니다. 주문 상태: %s", order.getOrderStatus()),
                orderId,
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

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        // 1. 취소 가능한 주문 조회
        Order order = orderRepository.findCancellableOrder(orderId, userId)
                .orElseThrow(() -> new RuntimeException("취소할 수 없는 주문입니다. 주문 후 30분이 경과했거나 존재하지 않는 주문입니다."));

        // 2. 주문 상태 CANCELLED로 변경
        boolean updated = orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);
        if (!updated) {
            throw new RuntimeException("주문 취소 실패: 상태 업데이트가 되지 않았습니다.");
        }

        // 3. 쿠폰 상태 복구
        restoreUserCoupon(order);

        // 4. 주문 아이템 조회 및 재고 복구
        List<OrderItemInfo> orderItems = orderRepository.findOrderItems(orderId);
        restoreRentalItemsStock(orderItems);

        // 5. 응답 데이터 준비
        return createOrderCancellationResponse(order, orderItems);
    }

    private void restoreUserCoupon(Order order) {
        if (order.getCouponId() != null) {
            UserCoupon userCoupon = userCouponRepository.findByCouponIdAndUserId(order.getCouponId(), order.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자의 쿠폰을 찾을 수 없습니다. CouponId: " + order.getCouponId() + ", UserId: " + order.getUserId()));

            userCoupon.setIsUsed(false);
            userCouponRepository.save(userCoupon);
        }
    }

    private void restoreRentalItemsStock(List<OrderItemInfo> orderItems) {
        for (OrderItemInfo orderItem : orderItems) {
            RentalItem rentalItem = rentalItemRepository.findById(orderItem.getRentalItemId())
                    .orElseThrow(() -> new RuntimeException("RentalItem을 찾을 수 없습니다. ID: " + orderItem.getRentalItemId()));

            rentalItem.setStock(rentalItem.getStock() + orderItem.getQuantity());
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
}




