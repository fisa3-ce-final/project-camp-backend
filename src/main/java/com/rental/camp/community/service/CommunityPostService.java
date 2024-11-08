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
    String saveImageFile(MultipartFile file) throws Exception;
    CommunityPostResponseDto updatePost(Long postId, CommunityPostUpdateRequestDto updateRequestDto, List<MultipartFile> newImages) throws Exception;
    void softDeletePost(Long postId, Long userId) throws AccessDeniedException;
    CommunityPostResponseDto getPostDetail(Long id);
    PageResponseDto getFreePosts(int page, int size);
    List<String> retrieveImagePaths(Long postId);
    boolean toggleLike(Long postId, Long userId);

}
