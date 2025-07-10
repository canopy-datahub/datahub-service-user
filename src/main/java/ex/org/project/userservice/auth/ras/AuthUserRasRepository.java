package ex.org.project.userservice.auth.ras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthUserRasRepository extends JpaRepository<AuthUserRas, Integer> {

    List<AuthUserRas> findAllByUserId(Integer userId);

    void deleteByUserId(Integer userId);

    @Query("select distinct new AuthUserRas(ur.userId, ur.passport) from AuthUserRas ur")
    List<AuthUserRas> findDistictUsers();

}
