package org.run.runx3.psh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {
    private Long userId;
    private Long friendId;
    private String status;  // PENDING, ACCEPTED, BLOCKED

}
