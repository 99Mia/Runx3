package org.run.runx3.psh.dto.page;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class PageResponseDTO<E> {
    private int page;   // 현재페이지
    private int size;   // 블록 사이즈
    private int total;  // 전체 레코드 수
    private int start; // 블록의 시작페이지
    private int end;    // 블록의 end 페이지
    private boolean next; // 다음 페이지 여부
    private boolean prev; // 이전페이지 여부
    private List<E> dtoList;
    private int totalPages;


    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(PageRequestDTO pageRequestDTO, List<E> dtoList, int total){
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.dtoList = dtoList;

        int totalPages = (int)Math.ceil(total / (double)size);
        this.totalPages = totalPages;

        int blockSize = 3;

        this.end = (int)(Math.ceil(this.page / (double)blockSize) * blockSize);
        if(this.end > totalPages) this.end = totalPages;

        this.start = this.end - (blockSize - 1);
        if(this.start < 1) this.start = 1;

        // 실제 페이지 기준
        this.prev = this.page > 1;
        this.next = this.page < totalPages;
    }
}









































