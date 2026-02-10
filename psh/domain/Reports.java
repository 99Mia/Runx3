package org.run.runx3.psh.domain;

import jakarta.persistence.*;
import lombok.*;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Comments;
import org.run.runx3.common.domain.Users;

@Entity
@Table(name="reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private String reason;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;  // default : PENDING

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Boards boards;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comments comment;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private Users reporter;

    // enum
    public enum Status {
        PENDING,
        RESOLVED
    }

}