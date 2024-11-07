package com.rental.camp.order.service;

import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.order.dto.OrderConflictDTO;
import com.rental.camp.order.dto.OrderRequestDTO;
import com.rental.camp.order.dto.OrderResponseDTO;
import com.rental.camp.order.model.CartItem;
import com.rental.camp.order.model.Order;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.order.repository.CartItemRepository;
import com.rental.camp.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        // 대여 기간 중복 여부 확인
        List<OrderConflictDTO> conflicts = checkForConflicts(requestDTO);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("예약 불가 - 중복된 대여 기간이 있습니다.");
        }

        // 주문 생성
        Order order = new Order();
        order.setUserId(requestDTO.getUserId());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setRentalDate(requestDTO.getRentalDate());
        order.setReturnDate(requestDTO.getReturnDate());
        order.setCouponId(requestDTO.getUserCouponId()); // 쿠폰 ID 설정
        order.setCreatedAt(LocalDateTime.now());

        // 주문을 먼저 저장하여 ID가 할당되도록 함
        orderRepository.save(order);

        // 총 금액 계산
        BigDecimal totalAmount = calculateTotalAmount(order, requestDTO.getCartItemIds(), requestDTO.getUserCouponId());

        // 총 금액 설정 및 다시 저장
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        return new OrderResponseDTO(requestDTO.getUserId(), "예약이 성공적으로 생성되었습니다.");
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

    private List<OrderConflictDTO> checkForConflicts(OrderRequestDTO requestDTO) {
        List<OrderConflictDTO> conflicts = new ArrayList<>();
        for (Long cartItemId : requestDTO.getCartItemIds()) {
            conflicts.addAll(orderRepository.findConflictingOrdersWithItemNames(cartItemId, requestDTO.getRentalDate(), requestDTO.getReturnDate()));
        }
        return conflicts;
    }
}
