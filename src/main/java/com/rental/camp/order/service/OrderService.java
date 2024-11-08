package com.rental.camp.order.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.order.dto.OrderConflict;
import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.dto.OrderResponse;
import com.rental.camp.order.model.*;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.order.repository.CartItemRepository;
import com.rental.camp.order.repository.OrderItemRepository;
import com.rental.camp.order.repository.OrderRepository;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.RentalItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final JPAQueryFactory queryFactory;

    @Transactional
    public OrderResponse createOrder(OrderRequest requestDTO) {
        // 대여 기간 중복 여부 확인
        List<OrderConflict> conflicts = checkForConflicts(requestDTO);
        if (!conflicts.isEmpty()) {
            // 중복된 아이템 정보를 포함한 응답 반환
            return new OrderResponse(requestDTO.getUserId(), "예약 불가 - 중복된 대여 기간이 있습니다.", conflicts);
        }

        // 주문 생성
        Order order = new Order();
        order.setUserId(requestDTO.getUserId());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setRentalDate(requestDTO.getRentalDate());
        order.setReturnDate(requestDTO.getReturnDate());
        order.setCouponId(requestDTO.getUserCouponId()); // 쿠폰 ID 설정
        // order.setCreatedAt(LocalDateTime.now());


        // 총 금액 계산
        BigDecimal totalAmount = calculateTotalAmount(order, requestDTO.getCartItemIds(), requestDTO.getUserCouponId());

        // 총 금액 설정 및 다시 저장
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        // 주문 아이템 생성 및 저장
        List<CartItem> cartItems = cartItemRepository.findAllById(requestDTO.getCartItemIds());

        // RentalItem 정보를 QueryDSL로 조회하여 가격 정보 가져오기
        List<Long> rentalItemIds = cartItems.stream()
                .map(CartItem::getRentalItemId)
                .collect(Collectors.toList());

        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<RentalItem> rentalItems = queryFactory
                .selectFrom(rentalItem)
                .where(rentalItem.id.in(rentalItemIds))
                .fetch();

        Map<Long, RentalItem> rentalItemMap = rentalItems.stream()
                .collect(Collectors.toMap(RentalItem::getId, Function.identity()));

        for (CartItem cartItem : cartItems) {
            RentalItem item = rentalItemMap.get(cartItem.getRentalItemId());
            if (item == null) {
                throw new RuntimeException("RentalItem을 찾을 수 없습니다: " + cartItem.getRentalItemId());
            }

            BigDecimal price = item.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setRentalItemId(cartItem.getRentalItemId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(price);
            orderItem.setSubtotal(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            //orderItem.setCreatedAt(LocalDateTime.now());
            orderItemRepository.save(orderItem);
        }

        return new OrderResponse(requestDTO.getUserId(), "예약이 성공적으로 생성되었습니다.");
    }

    private BigDecimal calculateTotalAmount(Order order, List<Long> cartItemIds, Long userCouponId) {
        long rentalDays = Duration.between(order.getRentalDate(), order.getReturnDate()).toDays();
        List<BigDecimal> itemPrices = cartItemRepository.findRentalItemPricesByCartItemIds(cartItemIds);

        BigDecimal totalAmount = IntStream.range(0, itemPrices.size())
                .mapToObj(i -> {
                    BigDecimal itemPrice = itemPrices.get(i);
                    CartItem cartItem = cartItemRepository.findById(cartItemIds.get(i))
                            .orElseThrow(() -> new RuntimeException("CartItem을 찾을 수 없습니다: " + cartItemIds.get(i)));
                    int quantity = cartItem.getQuantity();
                    return itemPrice.multiply(BigDecimal.valueOf(quantity)).multiply(BigDecimal.valueOf(rentalDays));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 쿠폰 할인 적용
        if (userCouponId != null) {
            Coupon coupon = couponRepository.findById(userCouponId)
                    .orElseThrow(() -> new RuntimeException("Coupon을 찾을 수 없습니다: " + userCouponId));
            totalAmount = applyCouponDiscount(totalAmount, coupon);
        }

        return totalAmount;
    }

    private BigDecimal applyCouponDiscount(BigDecimal totalAmount, Coupon coupon) {
        switch (coupon.getType()) {
            case PERCENTAGE_DISCOUNT:
                return totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscount()));
            case FIXED_AMOUNT_DISCOUNT:
                return totalAmount.subtract(coupon.getDiscount()).max(BigDecimal.ZERO);
            default:
                return totalAmount;
        }
    }

    private List<OrderConflict> checkForConflicts(OrderRequest requestDTO) {
        // 1. 요청한 CartItem으로부터 rentalItemId 목록 조회
        List<CartItem> cartItems = cartItemRepository.findAllById(requestDTO.getCartItemIds());
        List<Long> requestedRentalItemIds = cartItems.stream()
                .map(CartItem::getRentalItemId)
                .collect(Collectors.toList());

        // 2. 대여 기간이 겹치는 주문의 ID 목록 조회
        QOrder order = QOrder.order;

        List<Long> overlappingOrderIds = queryFactory
                .select(order.id)
                .from(order)
                .where(
                        order.orderStatus.ne(OrderStatus.CANCELLED)
                                .and(order.rentalDate.lt(requestDTO.getReturnDate()))
                                .and(order.returnDate.gt(requestDTO.getRentalDate()))
                )
                .fetch();

        if (overlappingOrderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 겹치는 주문의 OrderItem 중에서 요청한 rentalItemId와 겹치는 것 조회
        QOrderItem orderItem = QOrderItem.orderItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<OrderConflict> conflicts = queryFactory
                .select(Projections.constructor(OrderConflict.class,
                        rentalItem.name,
                        order.returnDate))
                .from(orderItem)
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id))
                .join(order).on(orderItem.orderId.eq(order.id))
                .where(
                        orderItem.rentalItemId.in(requestedRentalItemIds)
                                .and(orderItem.orderId.in(overlappingOrderIds))
                )
                .fetch();

        return conflicts;
    }
}
