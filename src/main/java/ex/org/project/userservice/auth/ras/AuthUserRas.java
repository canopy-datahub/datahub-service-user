package ex.org.project.userservice.auth.ras;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "user_ras")
public class AuthUserRas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="phs")
    private String phs;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "expiration_dt")
    private ZonedDateTime expirationDate;

    @Column(name = "create_at")
    private ZonedDateTime createdAt;

    @Column(name = "passport")
    private String passport;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = ZonedDateTime.now();
    }

    public AuthUserRas(Integer userId, String passport){
        this.userId = userId;
        this.passport = passport;
    }

    public AuthUserRas(Integer id, String phs, Integer userId){
        this.id = id;
        this.phs = phs;
        this.userId = userId;
    }

}
