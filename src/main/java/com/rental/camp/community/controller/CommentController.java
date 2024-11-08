package com.rental.camp.community.controller;

import com.rental.camp.community.dto.CommentRequestDto;
import com.rental.camp.community.dto.CommentResponseDto;
import com.rental.camp.community.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequestDto commentRequestDto) {
        CommentResponseDto response = commentService.addComment(postId, commentRequestDto);
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 조회
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByPostId(
            @PathVariable("postId") Long postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<CommentResponseDto> comments = commentService.getCommentsByPostId(postId, page - 1, size);
        return ResponseEntity.ok(comments);
    }

}
