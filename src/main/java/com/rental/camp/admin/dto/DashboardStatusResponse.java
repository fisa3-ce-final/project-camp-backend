package com.rental.camp.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DashboardStatusResponse {
    private Integer totalItems;     // 전체 등록 물품 수
    private Integer pendingReviews; // 심사 대기 수
    private Integer rentalRequests; // 물품 대여 신청 수
    private Integer overdueItems;   // 물품 연체 수
    private List<MonthData> monthDataList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class MonthData {
        private Integer month;  // 월
        private Integer count;  // 해당 월의 등록 물품 수
    }
}
