package com.rental.camp.rental.service;

import com.rental.camp.rental.dto.*;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.RentalItemImage;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import com.rental.camp.rental.repository.RentalItemImageRepository;
import com.rental.camp.rental.repository.RentalItemRepository;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RentalItemService {
    private final RentalItemRepository rentalItemRepository;
    private final RentalItemImageRepository rentalItemImageRepository;
    private final UserRepository userRepository;

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

    public void createRentalItem(String uuid, RentalItemCreateRequest request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();

        RentalItem rentalItem = RentalItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .status(String.valueOf(RentalItemStatus.AVAILABLE))
                .viewCount(0)
                .ratingAvg(BigDecimal.ZERO)
                .userId(userId)
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

    // 마이페이지에서 내 대여 기록 조회
    public Page<MyRentalItemsResponse> getMyRentalItems(String uuid, MyPageRequest request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return rentalItemRepository.findByRentalItemsUserId(userId, PageRequest.of(request.getPage(), request.getSize()));
    }
}
