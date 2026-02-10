package org.run.runx3.psh.domain;

import jakarta.persistence.*;
import lombok.*;
import org.run.runx3.common.domain.Users;

@Entity
@Table(name = "friends")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private Users friend;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, ACCEPTED, BLOCKED
    }
}
