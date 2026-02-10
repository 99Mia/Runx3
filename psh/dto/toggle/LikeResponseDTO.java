package org.run.runx3.psh.dto.toggle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDTO {
    private boolean liked;   // 좋아요 상태
    private int likeCount;   // 총 좋아요 수
}
