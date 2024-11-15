package com.rental.camp.community.controller;

import com.rental.camp.community.dto.CommunityPostRequestDto;
import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.dto.CommunityPostUpdateRequestDto;
import com.rental.camp.community.dto.PageResponseDto;
import com.rental.camp.community.service.CommunityPostService;
import com.rental.camp.coupon.dto.CouponResponse;
import com.rental.camp.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityPostController {

    private final CommunityPostService postService;
    private final CouponService couponService;

//    public CommunityPostController(CommunityPostService postService) {
//        this.postService = postService;
//    }

    @PostMapping
    public ResponseEntity<CommunityPostResponseDto> createPost(@ModelAttribute CommunityPostRequestDto requestDto) throws Exception {
        CommunityPostResponseDto response = postService.createPost(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created 반환
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityPostResponseDto> getPostDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        CommunityPostResponseDto postDetail = postService.getPostDetail(id, page, size);
        return ResponseEntity.ok(postDetail);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommunityPostResponseDto> updatePost(
            @PathVariable("id") Long id,
            @ModelAttribute CommunityPostUpdateRequestDto updateRequestDto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) throws Exception {

        CommunityPostResponseDto updatedPost = postService.updatePost(id, updateRequestDto, newImages);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) throws AccessDeniedException {

        postService.softDeletePost(id, userId);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        boolean isLiked = postService.toggleLike(id, userId);
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping("/free")
    public ResponseEntity<PageResponseDto> getFreePosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        PageResponseDto response = postService.getFreePosts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/review")
    public ResponseEntity<PageResponseDto> getReviewPosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        PageResponseDto response = postService.getReviewPosts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/{searchParam}")
    public ResponseEntity<List<CommunityPostResponseDto>> searchPosts(@PathVariable("searchParam") String searchParam) {
        List<CommunityPostResponseDto> searchResults = postService.searchPosts(searchParam);
        return ResponseEntity.ok(searchResults);
    }


    @GetMapping("/coupon")
    public ResponseEntity<Page<CouponResponse>> getActiveCoupons(Pageable pageable) {
        Page<CouponResponse> coupons = couponService.getAllActiveCoupons(pageable);
        return ResponseEntity.ok(coupons);
    }
}