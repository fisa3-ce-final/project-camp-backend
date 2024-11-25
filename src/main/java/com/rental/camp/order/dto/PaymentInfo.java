package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {

    private Long orderId;
    private String orderName;
    private String successUrl;
    private String failUrl;
    private String customerEmail;
    private String customerName;
    private String customerMobilePhone;
}
