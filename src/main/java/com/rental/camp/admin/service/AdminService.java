package com.rental.camp.admin.service;

import com.rental.camp.admin.dto.AuditRequest;
import com.rental.camp.admin.dto.AuditResponse;
import com.rental.camp.admin.dto.DashboardStatusResponse;
import com.rental.camp.admin.dto.UpdateStatusRequest;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.type.RentalItemStatus;
import com.rental.camp.rental.model.type.RentalStatus;
import com.rental.camp.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class AdminService {
    private final RentalItemRepository rentalItemRepository;

    public Page<AuditResponse> getAuditList(AuditRequest request) {
        Page<RentalItem> rentalItems;

        if (request.getStatus() == RentalItemStatus.ALL) {
            rentalItems = rentalItemRepository.findAll(PageRequest.of(request.getPage(), request.getSize()));
        } else {
            rentalItems = rentalItemRepository.findItemsByStatus(request.getStatus(), PageRequest.of(request.getPage(), request.getSize()));
        }

        return rentalItems.map(item -> {
            AuditResponse response = new AuditResponse();

            response.setRentalItemId(item.getId());
            response.setName(item.getName());
            response.setCategory(item.getCategory());
            response.setPrice(item.getPrice());
            response.setStatus(item.getStatus());
            response.setCreatedAt(item.getCreatedAt());

            return response;
        });
    }

    @Transactional
    public RentalItemStatus reviewAudit(Long id, UpdateStatusRequest request) {
        rentalItemRepository.findById(id).ifPresent(item -> {
            item.setStatus(request.getStatus());
        });

        return rentalItemRepository.findById(id).get().getStatus();
    }

    public DashboardStatusResponse getDashboardStatus() {
        List<DashboardStatusResponse.MonthData> monthDataList = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> DashboardStatusResponse.MonthData.builder()
                        .month(month)
                        .count(rentalItemRepository.countByMonth(month))
                        .build())
                .collect(Collectors.toList());

        return DashboardStatusResponse.builder()
                .totalItems((int) rentalItemRepository.count())
                .pendingReviews(rentalItemRepository.countByRentalItemStatus(RentalItemStatus.PENDING))
                .rentalRequests(rentalItemRepository.countByRentalStatus(RentalStatus.RENTED))
                .overdueItems(rentalItemRepository.countByRentalStatus(RentalStatus.OVERDUE))
                .monthDataList(monthDataList)
                .build();
    }
}
