package ex.org.project.userservice.auth.ras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserLoginRepository extends JpaRepository<AuthUserLogin, Integer> {

}
