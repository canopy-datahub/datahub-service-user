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
@Table(name = "user_login")
public class AuthUserLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "login_at")
    private ZonedDateTime loginAt;

    @PrePersist
    public void setLoginAt() {
        this.loginAt = ZonedDateTime.now();
    }

    public AuthUserLogin(Integer userId) {
        this.userId = userId;
    }
}
