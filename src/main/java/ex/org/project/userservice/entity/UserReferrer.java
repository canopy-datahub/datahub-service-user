package ex.org.project.userservice.entity;

import ex.org.project.userservice.dto.ReferrerSelectionDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_referrer")
public class UserReferrer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "user_id")
    private Integer userId;

    @NotNull
    @Column(name = "referrer_id")
    private Integer referrerId;

    @Size(max = 1024)
    @Column(name = "referrer_specify", length = 1024)
    private String referrerSpecify;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    public UserReferrer(ReferrerSelectionDTO referrerSelectionDTO, Integer userId) {
        this.userId = userId;
        this.referrerId = referrerSelectionDTO.getReferrerId();
        this.referrerSpecify = referrerSelectionDTO.getReferrerSpecify();
        this.createdAt = Instant.now();
        this.createdBy = userId;
    }

}