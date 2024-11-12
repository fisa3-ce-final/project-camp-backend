package com.rental.camp.rental.model.type;

public enum RentalItemStatus {
    PENDING,    // 심사 대기 상태
    APPROVED,   // 심사 승인
    REJECTED,    // 심사 반려
    AVAILABLE,  // 대여 가능
    UNAVAILABLE // 대여 불가능
}
