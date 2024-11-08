package com.rental.camp.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "comment")
public class Comment {

    @Id
    @SequenceGenerator(
            name = "comment_id_seq",
            sequenceName = "comment_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "comment_id_seq"
    )
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(name = "community_post_id", nullable = false)
    private Long communityPostId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
