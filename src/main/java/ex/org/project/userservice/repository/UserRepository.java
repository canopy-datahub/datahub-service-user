package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LookupStatus;
import ex.org.project.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String emailAddress);

    boolean existsByEmail(String emailAddress);

    List<User> findByStatus_NameAndStatus_Usage(String statusName, String statusUsage);

    List<User> findByStatusOrderByCreatedAtDesc(LookupStatus status);
}
