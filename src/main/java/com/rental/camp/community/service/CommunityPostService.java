package com.rental.camp.community.service;

import com.rental.camp.community.dto.CommunityPostRequestDto;
import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.dto.CommunityPostUpdateRequestDto;
import com.rental.camp.community.dto.PageResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface CommunityPostService {

    CommunityPostResponseDto createPost(CommunityPostRequestDto requestDto) throws Exception;
    CommunityPostResponseDto getPostDetail(Long id, int page, int size);
    CommunityPostResponseDto updatePost(Long postId, CommunityPostUpdateRequestDto updateRequestDto, List<MultipartFile> newImages) throws Exception;
    void softDeletePost(Long postId, Long userId) throws AccessDeniedException;
    boolean toggleLike(Long postId, Long userId);
    PageResponseDto getFreePosts(int page, int size);
    PageResponseDto getReviewPosts(int page, int size);
    List<CommunityPostResponseDto> searchPosts(String searchParam);
}
