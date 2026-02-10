package org.run.runx3.psh.dto.toggle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    private Long boardId;
    private Long userId;
    private String reason;   // 신고 사유
}
