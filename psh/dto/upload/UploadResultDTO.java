package org.run.runx3.psh.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDTO {
    private String uuid;   // 저장된 파일의 UUID(중복 방지용)
    private String fileName;  // 원본 파일명
    private boolean image;  // 이미지 여부(true = 이미지 파일)

    // 저장된 파일 경로를 반환하는 메서드
    public String getLink() {
        if (image) {
            return "s_" + uuid + "_" + fileName;    // 썸네일 이미지 경로
        } else {
            return uuid + "_" + fileName;           // 일반 파일 경로
        }
    }
}
