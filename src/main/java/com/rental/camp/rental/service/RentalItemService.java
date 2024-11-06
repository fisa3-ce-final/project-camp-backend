package com.rental.camp.rental.service;

import com.rental.camp.rental.dto.RentalItemRequestDto;
import com.rental.camp.rental.dto.RentalItemResponseDto;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentalItemService {
    private final RentalItemRepository rentalItemRepository;

    public Page<RentalItemResponseDto> getRentalItems(RentalItemRequestDto requestDto) {
        Page<RentalItem> rentalItems = rentalItemRepository.findAvailableItems(PageRequest.of(requestDto.getPage(), requestDto.getSize()));

        return rentalItems.map(item -> {
            RentalItemResponseDto responseDto = new RentalItemResponseDto();

            responseDto.setId(item.getId());
            responseDto.setName(item.getName());
            responseDto.setPrice(item.getPrice());
            responseDto.setStock(item.getStock());
            responseDto.setCategory(item.getCategory());
            responseDto.setStatus(item.getStatus());

            return responseDto;
        });
    }
}
