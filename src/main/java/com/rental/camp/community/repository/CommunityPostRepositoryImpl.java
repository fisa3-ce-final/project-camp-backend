package com.rental.camp.community.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.community.model.CommunityPost;
import com.rental.camp.community.model.QCommunityPost;
import com.rental.camp.community.model.type.CommunityPostCategory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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

}
