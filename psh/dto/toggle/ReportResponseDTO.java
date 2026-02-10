package org.run.runx3.psh.dto.toggle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private boolean reported;  // 신고 성공 여부
    private int reportCount;   // 총 신고 수
}
