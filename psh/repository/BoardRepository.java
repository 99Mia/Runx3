package org.run.runx3.psh.repository;

import org.run.runx3.common.domain.Boards;
import org.run.runx3.psh.repository.search.BoardSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Boards,Long>, BoardSearch {
    @EntityGraph(attributePaths = {"imageSet", "users"}) // 이미지와 작성자 정보 같이 조회
    @Query("select b from Boards b left join fetch b.imageSet left join fetch b.users where b.boardId = :boardId")
    Optional<Boards> findByIdWithImages(@Param("boardId") Long boardId);

    @Query("select b from Boards b join fetch b.users where b.boardId = :boardId")
    Optional<Boards> findByIdWithUser(@Param("boardId") Long boardId);


    @Query("SELECT b FROM Boards b LEFT JOIN Likes l ON l.boards = b GROUP BY b.boardId ORDER BY COUNT(l) DESC")
    Page<Boards> findAllOrderByLikes(Pageable pageable);

    List<Boards> findByUsers_UserId(Long userId);
    List<Boards> findByCategory(Boards.Category category);

}

