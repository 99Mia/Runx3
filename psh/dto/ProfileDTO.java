package org.run.runx3.psh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long profileId;
    private Long userId;
    private String bio;
    private double totalDistance;
    private double totalTime;
    private double recent4WeeksDistance;
}
