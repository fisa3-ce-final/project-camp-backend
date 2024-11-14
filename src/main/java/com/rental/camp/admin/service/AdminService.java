package com.rental.camp.admin.service;

import com.rental.camp.admin.dto.AuditRequest;
import com.rental.camp.admin.dto.AuditResponse;
import com.rental.camp.admin.dto.UpdateStatusRequest;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.type.RentalItemStatus;
import com.rental.camp.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
