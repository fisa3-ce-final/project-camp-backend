package com.rental.camp.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {

    private String content; // 댓글 내용
    private Long userId;    // 작성자 ID
}
