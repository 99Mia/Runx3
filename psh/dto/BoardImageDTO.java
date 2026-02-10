package org.run.runx3.psh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardImageDTO {
    private String uuid;
    private String filename;
    private int ord;
    private boolean image;
    private String savePath;  // 서버에 저장된 경로
    private String thumbnail;
}
