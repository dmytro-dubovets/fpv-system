package ua.fpv.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "fpv_pilot")
public class FpvPilot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "fpv_pilot_id")
    private Long fpvPilotId;

    @Column(name = "first_name")
    @NotBlank(message = "Username is required")
    private String firstname;

    @Column(name = "last_name")
    @NotBlank(message = "Lastname is required")
    private String lastname;

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;
}
