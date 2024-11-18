package com.rental.camp.rental.service;

import com.rental.camp.global.config.S3Client;
import com.rental.camp.rental.dto.*;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.RentalItemImage;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.repository.RentalItemImageRepository;
import com.rental.camp.rental.repository.RentalItemRepository;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class RentalItemService {
    private final RentalItemRepository rentalItemRepository;
    private final RentalItemImageRepository rentalItemImageRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;

    public Page<RentalItemResponse> getRentalItems(RentalItemCategory category, RentalItemRequest requestDto) {
        Page<RentalItem> rentalItems = rentalItemRepository.findAvailableItemsByType(category, PageRequest.of(requestDto.getPage(), requestDto.getSize()));

        return rentalItems.map(item -> {
            RentalItemResponse responseDto = new RentalItemResponse();
            Long userId = item.getUserId();
            Optional<User> user = userRepository.findById(userId);

            List<RentalItemImage> images = rentalItemImageRepository.findByRentalItemId(item.getId());
            String rentalItemImageUrl = images.isEmpty() ? "이미지 없음" : images.get(0).getImageUrl();

            responseDto.setNickname(user.get().getNickname());
            responseDto.setUserImageUrl(user.get().getImageUrl());
            responseDto.setRentalId(item.getId());
            responseDto.setRentalImageUrl(rentalItemImageUrl);
            responseDto.setRentalItemName(item.getName());
            responseDto.setPrice(item.getPrice());
            responseDto.setStock(item.getStock());
            responseDto.setCategory(item.getCategory());
            responseDto.setStatus(item.getStatus());
            responseDto.setRatingAvg(item.getRatingAvg());

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
                .category(RentalItemCategory.valueOf(request.getCategory().toUpperCase()))
                .viewCount(0)
                .ratingAvg(BigDecimal.ZERO)
                .userId(userId)
                .build();

        rentalItemRepository.save(rentalItem);

        AtomicInteger order = new AtomicInteger(0);

        List<RentalItemImage> images = request.getImages().stream()
                        .map(imgDto -> {
                            String imageUrl = null;
                            try {
                                imageUrl = s3Client.uploadImage("rental-item/" + uuid + "/", imgDto);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            return RentalItemImage.builder()
                                    .imageUrl(imageUrl)
                                    .imageOrder(order.getAndIncrement())
                                    .rentalItemId(rentalItem.getId())
                                    .build();
                        })
                        .toList();

        rentalItemImageRepository.saveAll(images);
    }

    // 마이페이지에서 내 대여 기록 조회
    public Page<MyRentalItemsResponse> getMyRentalItems(String uuid, MyPageRequest request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return rentalItemRepository.findRentalItemsByUserId(userId, PageRequest.of(request.getPage(), request.getSize()));
    }

    // 마이페이지에서 내가 빌려준 물품 목록 조회
    public Page<MyItemsResponse> getMyItems(String uuid, MyPageRequest pageRequest) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return rentalItemRepository.findItemsByUserId(userId, PageRequest.of(pageRequest.getPage(), pageRequest.getSize()));
    }

    // 마이페이지에서 내 주문 내역 조회
    public Page<MyOrdersResponse> getMyOrders(String uuid, MyPageRequest pageRequest) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return rentalItemRepository.findOrdersByUserId(userId, PageRequest.of(pageRequest.getPage(), pageRequest.getSize()));
    }
}
