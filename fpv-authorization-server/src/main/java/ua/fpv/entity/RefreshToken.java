package ua.fpv.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "fpv_pilot_id", referencedColumnName = "fpv_pilot_id", nullable = false)
    private FpvPilot fpvPilot;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken refreshToken)) return false;
        if (this.id == null || refreshToken.id == null) return false;

        return Objects.equals(this.id, refreshToken.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
