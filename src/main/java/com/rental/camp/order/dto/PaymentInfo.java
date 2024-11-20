package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {
    Long orderId;
    String orderName;
    String successUrl;
    String failUrl;
    String customerEmail;
    String customerName;
    String customerMobilePhone;
}
