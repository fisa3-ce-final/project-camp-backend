package com.rental.camp.community.repository;

import com.rental.camp.community.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentRepositoryCustom {

    Optional<Comment> findCustomCommentByCommunityPostIdAndId(Long communityPostId, Long id);
    Page<Comment> findCustomCommentsByCommunityPostIdAndIsDeletedFalse(Long communityPostId, Pageable pageable);
}
