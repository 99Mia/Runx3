package org.run.runx3.psh.domain;

import jakarta.persistence.*;
import lombok.*;
import org.run.runx3.common.domain.Boards;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardImage implements Comparable<BoardImage> {

    @Id
    private String uuid;
    private String filename;
    private int ord;
    private boolean image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bno")
    private Boards boards;

    @Transient
    private Boards boardsCommon; // common.domain.Boards 참조용, DB에는 저장 안 됨

    @Override
    public int compareTo(BoardImage other) {
        return this.ord - other.ord;
    }

    // ← 여기, BoardImage 클래스 안에 위치
    public void changeBoardsCommon(Boards boards) {
        this.boardsCommon = boards;
    }
}
