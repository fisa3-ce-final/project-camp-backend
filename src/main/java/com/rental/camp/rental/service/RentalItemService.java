package com.rental.camp.rental.service;

import com.rental.camp.rental.dto.RentalItemCreateRequest;
import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.dto.RentalItemRequest;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.RentalItemImage;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import com.rental.camp.rental.repository.RentalItemImageRepository;
import com.rental.camp.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RentalItemService {
    private final RentalItemRepository rentalItemRepository;
    private final RentalItemImageRepository rentalItemImageRepository;

    public Page<RentalItemResponse> getRentalItems(RentalItemCategory category, RentalItemRequest requestDto) {
        Page<RentalItem> rentalItems;

        if (category == RentalItemCategory.ALL) {
            // 모든 카테고리의 아이템을 조회
            rentalItems = rentalItemRepository.findAll(PageRequest.of(requestDto.getPage(), requestDto.getSize()));
        } else {
            // 특정 카테고리의 아이템을 조회
            rentalItems = rentalItemRepository.findAvailableItemsByType(category, PageRequest.of(requestDto.getPage(), requestDto.getSize()));
        }

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

    public void createRentalItem(RentalItemCreateRequest request) {
        RentalItem rentalItem = RentalItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .status(String.valueOf(RentalItemStatus.AVAILABLE))
                .viewCount(0)
                .ratingAvg(BigDecimal.ZERO)
                .userId(1L)
                .build();

        rentalItemRepository.save(rentalItem);

        List<RentalItemImage> images = request.getImages().stream()
                .map(imgDto -> RentalItemImage.builder()
                        .imageUrl(imgDto.getImageUrl())
                        .imageOrder(imgDto.getImageOrder())
                        .rentalItemId(rentalItem.getId())
                        .build())
                .toList();

        rentalItemImageRepository.saveAll(images);
    }
}
