package org.run.runx3.psh.repository.search;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.QBoards;
import org.run.runx3.common.domain.QComments;
import org.run.runx3.psh.dto.BoardListCommentCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
@Log4j2
public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {
    public BoardSearchImpl() {
        super(Boards.class);
    }

    @Override
    public Page<Boards> search1(Pageable pageable) {
        QBoards qBoards = QBoards.boards;
        JPQLQuery<Boards> query = from(qBoards);

        BooleanBuilder builder = new BooleanBuilder();
        builder.or(qBoards.title.containsIgnoreCase("테스트"));
        builder.or(qBoards.content.containsIgnoreCase("테스트"));

        query.where(builder);
        query.where(qBoards.boardId.gt(0L));

        this.getQuerydsl().applyPagination(pageable, query);
        List<Boards> list = query.fetch();
        long count = query.fetchCount();

        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public Page<Boards> searchAll(String[] types, String keyword, Pageable pageable) {
        QBoards qBoards = QBoards.boards;
        JPQLQuery<Boards> query = from(qBoards);

        if (types != null && types.length > 0 && keyword != null) {
            BooleanBuilder builder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "t" -> builder.or(qBoards.title.containsIgnoreCase(keyword));
                    case "c" -> builder.or(qBoards.content.containsIgnoreCase(keyword));
                    case "w" -> builder.or(qBoards.users.username.containsIgnoreCase(keyword));
                }
            }
            query.where(builder);
        }

        query.where(qBoards.boardId.gt(0L));
        this.getQuerydsl().applyPagination(pageable, query);

        List<Boards> list = query.fetch();
        long count = query.fetchCount();

        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public Page<BoardListCommentCountDTO> searchWithCommentCount(String[] types, String keyword, Pageable pageable) {
        log.info("searchWithCommentCount===========================================");
        QBoards qBoards = QBoards.boards;
        QComments qComment = QComments.comments;

        JPQLQuery<Boards> query = from(qBoards);
        query.leftJoin(qComment).on(qComment.boards.eq(qBoards));
        query.groupBy(qBoards.boardId);

        if (types != null && types.length > 0 && keyword != null) {
            BooleanBuilder builder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "t" -> builder.or(qBoards.title.containsIgnoreCase(keyword));
                    case "c" -> builder.or(qBoards.content.containsIgnoreCase(keyword));
                    case "w" -> builder.or(qBoards.users.username.containsIgnoreCase(keyword));
                }
            }
            query.where(builder);
        }

        query.where(qBoards.boardId.gt(0L));

        JPQLQuery<BoardListCommentCountDTO> dtoQuery = query.select(
                Projections.bean(
                        BoardListCommentCountDTO.class,
                        qBoards.boardId,
                        qBoards.title,
                        qBoards.users.username.as("username"),
                        qBoards.views,
                        qBoards.createdAt,
                        qComment.count().as("commentCount")
                )
        );

        this.getQuerydsl().applyPagination(pageable, dtoQuery);

        List<BoardListCommentCountDTO> dtoList = dtoQuery.fetch();
        log.info("searchWithCommentCount1==========================================="+dtoQuery.fetch().stream().count());
        long count = dtoQuery.fetchCount();
        log.info("searchWithCommentCount2==========================================="+count);

        return new PageImpl<>(dtoList, pageable, count);
    }
}




























