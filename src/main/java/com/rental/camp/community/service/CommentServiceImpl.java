package com.rental.camp.community.service;

import com.rental.camp.community.dto.CommentRequestDto;
import com.rental.camp.community.dto.CommentResponseDto;
import com.rental.camp.community.model.Comment;
import com.rental.camp.community.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long postId, CommentRequestDto commentRequestDto) {
        Comment comment = new Comment();
        comment.setCommunityPostId(postId);
        comment.setContent(commentRequestDto.getContent());
        comment.setUserId(commentRequestDto.getUserId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setIsDeleted(false);

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDto(
                savedComment.getId(),
                savedComment.getContent(),
                savedComment.getUserId(),
                savedComment.getCreatedAt(),
                savedComment.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findCustomCommentByCommunityPostIdAndId(postId, commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with postId: " + postId + " and commentId: " + commentId));

        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    public Page<CommentResponseDto> getCommentsByPostId(Long postId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return commentRepository.findCustomCommentsByCommunityPostIdAndIsDeletedFalse(postId, pageRequest)
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUserId(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt()
                ));
    }
}
