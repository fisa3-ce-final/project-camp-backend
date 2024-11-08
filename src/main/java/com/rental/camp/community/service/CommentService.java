package com.rental.camp.community.service;

import com.rental.camp.community.dto.CommentRequestDto;
import com.rental.camp.community.dto.CommentResponseDto;
import org.springframework.data.domain.Page;

public interface CommentService {

    CommentResponseDto addComment(Long postId, CommentRequestDto commentRequestDto);
    void deleteComment(Long postId, Long commentId);
    Page<CommentResponseDto> getCommentsByPostId(Long postId, int page, int size);
}
