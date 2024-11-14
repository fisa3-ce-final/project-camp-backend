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

    @GetMapping("/dashboard/status")
    public DashboardStatusResponse getDashboardStatus() {
        return adminService.getDashboardStatus();
    }

    @GetMapping("/rentals/{status}")
    public Page<RentalStatusResponse> getRentalList(@PathVariable String status,
                                                    @RequestParam int page, @RequestParam int size) {
        return adminService.getRentalList(RentalStatus.valueOf(status.toUpperCase()), page, size);
    }

    @GetMapping("/rental-items")
    public Page<AuditResponse> getAuditList(@RequestBody AuditRequest request) {
        return adminService.getAuditList(request);
    }

    @PutMapping("/rental-items/{id}")
    public RentalItemStatus reviewAudit(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return adminService.reviewAudit(id, request);
    }
}
