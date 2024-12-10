package com.rental.camp.rental.service;

import com.rental.camp.global.config.S3Client;
import com.rental.camp.order.repository.OrderItemRepository;
import com.rental.camp.rental.dto.*;
import com.rental.camp.rental.model.IpTracking;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.RentalItemImage;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.repository.IpTrackingRepository;
import com.rental.camp.rental.repository.RentalItemImageRepository;
import com.rental.camp.rental.repository.RentalItemRepository;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class RentalItemService {
    private final RentalItemRepository rentalItemRepository;
    private final RentalItemImageRepository rentalItemImageRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final IpTrackingRepository ipTrackingRepository;
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

    public void createRentalItem(String uuid, RentalItemCreateRequest request) throws IOException {
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

        List<CompletableFuture<String>> images =  new ArrayList<>();

        for (MultipartFile file : request.getImages()) {
            images.add(s3Client.uploadImage("rental-item/" + uuid + "/", file));
        }

        List<String> urls = new ArrayList<>();

        images.forEach(image -> urls.add(image.join()));

        AtomicInteger order = new AtomicInteger(0);

        List<RentalItemImage> rentalItemImages = urls.stream()
                .map(imageUrl -> RentalItemImage.builder()
                        .imageUrl(imageUrl)
                        .imageOrder(order.getAndIncrement())
                        .rentalItemId(rentalItem.getId())
                        .build())
                .toList();

        rentalItemImageRepository.saveAll(rentalItemImages);
    }

    public Page<RentalItemResponse> searchRentalItems(String keyword, RentalItemRequest requestDto) {
        Page<RentalItem> searchResult = rentalItemRepository.findItemsBySearchKeyword(keyword, PageRequest.of(requestDto.getPage(), requestDto.getSize()));

        return searchResult.map(item -> {
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

    @Transactional
    public String addViewNum(Long id, String clientIp) {
        rentalItemRepository.findById(id).ifPresent(rentalItem -> {
            if (!ipTrackingRepository.existsByIp(clientIp)) {
                rentalItem.setViewCount(rentalItem.getViewCount() + 1);
                ipTrackingRepository.save(IpTracking.builder()
                                .ip(clientIp)
                                .rentalItemId(id)
                        .build());
            }
        });

        return "조회수 증가 성공";
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

    // 별점 후기 남기기
    @Transactional
    public BigDecimal reviewUtilization(Long rentalItemId, String uuid, BigDecimal ratingAvg) {
        int reviewCount = orderItemRepository.countByRentalItemId(rentalItemId);

        BigDecimal newRatingAvg = ratingAvg.multiply(new BigDecimal(reviewCount)).add(ratingAvg).divide(new BigDecimal(reviewCount + 1), 2, RoundingMode.HALF_UP);

        rentalItemRepository.findById(rentalItemId).get().setRatingAvg(newRatingAvg);

        return newRatingAvg;
    }
}
