package com.rental.camp.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "community_like")
public class CommunityLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    public CommunityLike() {}

    public CommunityLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }
}