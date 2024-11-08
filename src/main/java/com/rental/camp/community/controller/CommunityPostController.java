package com.rental.camp.community.controller;

import com.rental.camp.community.dto.CommunityPostRequestDto;
import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.dto.CommunityPostUpdateRequestDto;
import com.rental.camp.community.dto.PageResponseDto;
import com.rental.camp.community.service.CommunityPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityPostController {

    private final CommunityPostService postService;

    public CommunityPostController(CommunityPostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<CommunityPostResponseDto> createPost(@ModelAttribute CommunityPostRequestDto requestDto) throws Exception {
        CommunityPostResponseDto response = postService.createPost(requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityPostResponseDto> getPostDetail(@PathVariable("id") Long id) {
        CommunityPostResponseDto postDetail = postService.getPostDetail(id);
        return ResponseEntity.ok(postDetail);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
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

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePost(
//            @PathVariable("id") Long id,
//            @RequestParam("userId") Long userId) throws AccessDeniedException {
//
//        postService.deletePost(id, userId);
//
//        return ResponseEntity.noContent().build(); // 204 No Content 반환
//    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        boolean isLiked = postService.toggleLike(id, userId);
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping("/free")
    public ResponseEntity<PageResponseDto> getFreePosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponseDto response = postService.getFreePosts(page, size);
        return ResponseEntity.ok(response);
    }
}
