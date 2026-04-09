package ex.org.project.userservice.auth.ras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRasTrackingRepository extends JpaRepository<AuthRasTracking, Integer> {

    Optional<AuthRasTracking> findBySessionId(String sessionId);

    Optional<AuthRasTracking> findRasTrackingBySessionId(String sessionId);

}
