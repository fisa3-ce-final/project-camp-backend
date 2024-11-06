package com.rental.camp.rental.service;

import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.dto.RentalItemRequest;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.repository.RentalItemRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentalItemService {
    private final RentalItemRepositoryImpl rentalItemRepository;

    public Page<RentalItemResponse> getRentalItems(RentalItemRequest requestDto) {
        Page<RentalItem> rentalItems = rentalItemRepository.findAvailableItems(PageRequest.of(requestDto.getPage(), requestDto.getSize()));

        return rentalItems.map(item -> {
            RentalItemResponse responseDto = new RentalItemResponse();

            responseDto.setId(item.getId());
            responseDto.setName(item.getName());
            responseDto.setPrice(item.getPrice());
            responseDto.setStock(item.getStock());
            responseDto.setCategory(item.getCategory());
            responseDto.setStatus(item.getStatus());

            return responseDto;
        });
    }

    public RentalItemDetailResponse getRentalItem(Long id) {
        return rentalItemRepository.findItemDetailById(id);
    }
}
