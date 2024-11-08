package com.rental.camp.community.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.community.model.Comment;
import com.rental.camp.community.model.QComment;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager; // EntityManager 추가
    private final QComment comment = QComment.comment; // QComment 객체 사용

    public CommentRepositoryImpl(JPAQueryFactory queryFactory, EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager; // EntityManager 초기화
    }

    @Override
    public Optional<Comment> findCustomCommentByCommunityPostIdAndId(Long communityPostId, Long id) {
        Comment foundComment = queryFactory
                .selectFrom(comment)
                .where(comment.communityPostId.eq(communityPostId).and(comment.id.eq(id)))
                .fetchOne();
        return Optional.ofNullable(foundComment);
    }

    @Override
    public Page<Comment> findCustomCommentsByCommunityPostIdAndIsDeletedFalse(Long communityPostId, Pageable pageable) {
        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .where(comment.communityPostId.eq(communityPostId).and(comment.isDeleted.isFalse()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.communityPostId.eq(communityPostId).and(comment.isDeleted.isFalse()))
                .fetchOne();

        return new PageImpl<>(comments, pageable, total);
    }
}
