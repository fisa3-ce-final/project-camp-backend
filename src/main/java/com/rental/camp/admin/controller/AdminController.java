package com.rental.camp.admin.controller;

import com.rental.camp.admin.dto.*;
import com.rental.camp.admin.service.AdminService;
import com.rental.camp.rental.model.type.RentalItemStatus;
import com.rental.camp.rental.model.type.RentalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final AdminService adminService;

    // 관리자 대시보드 전체 통계
    @GetMapping("/dashboard/status")
    public DashboardStatusResponse getDashboardStatus() {
        return adminService.getDashboardStatus();
    }

    // 관리자 대여 이용 현황 목록 조회
    @GetMapping("/rentals/{status}")
    public Page<RentalStatusResponse> getRentalList(@PathVariable(name = "status") String status,
                                                    @RequestParam(name = "page") int page, @RequestParam(name = "size") int size) {
        return adminService.getRentalList(RentalStatus.valueOf(status.toUpperCase()), page, size);
    }

    // 관리자 대여 이용 상태 수정
    @PutMapping("/rentals/{id}")
    public RentalStatus changeStatus(@PathVariable(name = "id") String id, @RequestBody RentalStatus status) {
        return adminService.changeStatus(id, status);
    }

    // 관리자 승인 목록 조회
    @GetMapping("/rental-items")
    public Page<AuditResponse> getAuditList(@ModelAttribute AuditRequest request) {
        return adminService.getAuditList(request);
    }

    // 관리자 승인 상태 수정
    @PutMapping("/rental-items/{id}")
    public String reviewAudit(@PathVariable(name = "id") Long id, @ModelAttribute UpdateStatusRequest request) {
        RentalItemStatus result = adminService.reviewAudit(id, request);
        return request.getStatus().toString() + " -> " + result.toString();
    }
}
