package org.run.runx3.psh.dto.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    @Builder.Default
    private int page=1;   // 현재 페이지 번호
    @Builder.Default
    private int size=10;   // 한 페이지에 보여줄 개수
    @Builder.Default
    private String direction = "desc";
    private String link;
    private String type;         // 검색 타입 (t,w,c)
    private String keyword;
    @Builder.Default
    private String sort = "";
    
    // 검색 타입 배열로 변환
    public String[] getTypes(){
        if(type==null || type.isEmpty()){
            return null;
        }
        return type.split("");    // 예 : twc => ["t","w","c"]
    }

    public Pageable getPageable(String sortField) {
        int pageNum = this.page <= 0 ? 1 : this.page;
        int pageSize = this.size <= 0 ? 10 : this.size;

        // sortField가 null이면 기본값 createdAt 사용
        String field = (sortField != null && !sortField.isEmpty()) ? sortField : "createdAt";

        Sort.Direction directionEnum = (this.direction != null && this.direction.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sortObj = Sort.by(directionEnum, field);

        return PageRequest.of(pageNum - 1, pageSize, sortObj);
    }


    // URL 쿼리 스트링 생성
    public String getLink(){
        if(link==null){
            StringBuilder builder = new StringBuilder();
            builder.append("page="+this.page);
            builder.append("&size="+this.size);
            if(type!=null && type.length()>0){
                builder.append("&type="+type);
            }
            if(keyword!=null && keyword.length()>0){
                builder.append("&keyword="+keyword);
            }
            link=builder.toString();
        }
        return link;
    }
    }

