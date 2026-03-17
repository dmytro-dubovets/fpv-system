package ua.fpv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "fpv_pilot")
@EntityListeners(AuditingEntityListener.class)
public class FpvPilot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "fpv_pilot_id")
    private Long fpvPilotId;

    @Column(name = "first_name")
    @NotBlank(message = "First name is required")
    private String firstname;

    @Column(name = "last_name")
    @NotBlank(message = "Lastname is required")
    private String lastname;

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fpv_pilot_authorities", joinColumns = @JoinColumn(name = "fpv_pilot_id"))
    @Column(name = "authority")
    private Set<String> authorities = new HashSet<>();

    @Column(name = "client_id")
    private String clientId;

    public void setAuthorities(Set<String> authorities) {
        this.authorities.clear();
        if (authorities != null) {
            this.authorities.addAll(authorities);
        }
    }

    public void addAuthority(String authority) {
        this.authorities.add(authority);
    }

    public void removeAuthority(String authority) {
        this.authorities.remove(authority);
    }

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false) // Створюється один раз, не змінюється
    @ToString.Exclude
    private FpvPilot createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by") // Оновлюється автоматично при кожному PUT запиті
    @ToString.Exclude
    private FpvPilot updatedBy;

    @OneToMany(mappedBy = "fpvPilot")
    @JsonIgnore
    private final List<FpvReport> fpvReports = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FpvPilot fpvPilot)) return false;
        if (this.fpvPilotId == null || fpvPilot.fpvPilotId == null) return false;
        return this.fpvPilotId.equals(fpvPilot.fpvPilotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fpvPilotId);
    }
}
