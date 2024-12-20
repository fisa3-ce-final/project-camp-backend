package com.rental.camp.community.repository;

import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.model.CommunityPost;
import com.rental.camp.community.model.type.CommunityPostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommunityPostRepositoryCustom {

    Page<CommunityPost> findByCategoryAndIsDeletedFalse(CommunityPostCategory category, Pageable pageable);
    Optional<CommunityPost> findByIdAndIsDeletedFalse(Long id);
    Page<CommunityPostResponseDto> findReviewPosts(Pageable pageable);
    List<CommunityPostResponseDto> searchPosts(String searchParam);
}
