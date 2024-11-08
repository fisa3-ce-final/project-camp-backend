package com.rental.camp.community.dto;

//import com.rental.camp.community.model.Comment;
import com.rental.camp.community.model.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommunityPostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Integer likes;
    private BigDecimal rating; // 후기 글에만 존재하는 필드
    private List<String> imagePaths; // 이미지 경로 목록 필드 추가

    public CommunityPostResponseDto(CommunityPost post, List<String> imagePaths) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.userId = post.getUserId();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.viewCount = post.getViewCount();
        this.likes = post.getLikes();
        this.rating = post.getRating(); // 후기 게시글일 경우 값이 존재
        this.imagePaths = imagePaths;   // 이미지 경로 설정
    }
}
