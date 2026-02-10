package org.run.runx3.psh.dto.toggle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponseDTO {
    private boolean bookmarked;  // 북마크 상태
    private int bookmarkCount;   // 총 북마크 수
}
