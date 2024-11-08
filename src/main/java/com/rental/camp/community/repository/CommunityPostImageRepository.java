package com.rental.camp.community.repository;

import com.rental.camp.community.model.CommunityPostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {

    List<CommunityPostImage> findByCommunityPostId(Long communityPostId);
    Optional<CommunityPostImage> findByImagePath(String imagePath);
    int countByCommunityPostId(Long communityPostId);
    List<CommunityPostImage> findByCommunityPostIdAndIsDeletedFalse(Long communityPostId);
}