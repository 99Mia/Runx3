package org.run.runx3.psh.repository.search;

import org.run.runx3.common.domain.Boards;
import org.run.runx3.psh.dto.BoardListCommentCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearch {
    // 단순 페이징 테스트용
    Page<Boards> search1(Pageable pageable);
    
    // 제목, 내용, 작성자 등으로 검색
    Page<Boards> searchAll(String[] types, String keyword, Pageable pageable);
    
    // 댓글 수 포함 목록
    Page<BoardListCommentCountDTO> searchWithCommentCount(String[] types,
                                                        String keyword,
                                                        Pageable pageable);
}
