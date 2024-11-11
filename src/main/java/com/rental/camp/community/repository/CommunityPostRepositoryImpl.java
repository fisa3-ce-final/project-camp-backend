package com.rental.camp.community.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.model.CommunityPost;
import com.rental.camp.community.model.QCommunityPost;
import com.rental.camp.community.model.type.CommunityPostCategory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.dsl.BooleanExpression;


import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class CommunityPostRepositoryImpl implements CommunityPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CommunityPostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<CommunityPost> findByCategoryAndIsDeletedFalse(CommunityPostCategory category, Pageable pageable) {
        QCommunityPost communityPost = QCommunityPost.communityPost;

        // CommunityPost 엔티티로 직접 조회
        List<CommunityPost> content = queryFactory
                .selectFrom(communityPost)
                .where(communityPost.category.eq(category)
                        .and(communityPost.isDeleted.eq(false)))
                .orderBy(communityPost.createdAt.desc(), communityPost.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(communityPost.count())
                .from(communityPost)
                .where(communityPost.category.eq(category)
                        .and(communityPost.isDeleted.eq(false)))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


    @Override
    public Optional<CommunityPost> findByIdAndIsDeletedFalse(Long id) {
        QCommunityPost communityPost = QCommunityPost.communityPost;

        CommunityPost result = queryFactory
                .selectFrom(communityPost)
                .where(communityPost.id.eq(id)
                        .and(communityPost.isDeleted.eq(false)))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<CommunityPostResponseDto> findReviewPosts(Pageable pageable) {
        QCommunityPost post = QCommunityPost.communityPost;

        List<CommunityPostResponseDto> content = queryFactory
                .select(Projections.fields(CommunityPostResponseDto.class,
                        post.id.as("id"),
                        post.title.as("title"),
                        post.content.as("content"),
                        Expressions.stringTemplate("cast({0} as string)", post.category).as("category"), // Enum을 문자열로 변환
                        post.userId.as("userId"),
                        post.createdAt.as("createdAt"),
                        post.updatedAt.as("updatedAt"),
                        post.viewCount.as("viewCount"),
                        post.likes.as("likes"),
                        post.rating.as("rating")
                ))
                .from(post)
                .where(post.category.eq(CommunityPostCategory.REVIEW))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(post.count())
                .from(post)
                .where(post.category.eq(CommunityPostCategory.REVIEW))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<CommunityPostResponseDto> searchPosts(String searchParam) {
        QCommunityPost post = QCommunityPost.communityPost;

        List<CommunityPostResponseDto> posts = queryFactory
                .select(Projections.fields(CommunityPostResponseDto.class,
                        post.id.as("id"),
                        post.title.as("title"),
                        post.content.as("content"),
                        Expressions.stringTemplate("cast({0} as string)", post.category).as("category"), // Enum을 문자열로 변환
                        post.userId.as("userId"),
                        post.createdAt.as("createdAt"),
                        post.updatedAt.as("updatedAt"),
                        post.viewCount.as("viewCount"),
                        post.likes.as("likes"),
                        post.rating.as("rating")
                ))
                .from(post)
                .where(titleContains(searchParam).or(contentContains(searchParam)))
                .fetch();

        return posts;
    }

    private BooleanExpression titleContains(String searchParam) {
        return QCommunityPost.communityPost.title.containsIgnoreCase(searchParam);
    }

    private BooleanExpression contentContains(String searchParam) {
        return QCommunityPost.communityPost.content.containsIgnoreCase(searchParam);
    }

}
