package com.rental.camp.admin.controller;

import com.rental.camp.admin.dto.AuditRequest;
import com.rental.camp.admin.dto.AuditResponse;
import com.rental.camp.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/rental-items")
    public Page<AuditResponse> getAuditList(@RequestBody AuditRequest request) {
        return adminService.getAuditList(request);
    }
}
