package ex.org.project.userservice.repository;

import java.util.List;
import java.util.Optional;

import ex.org.project.userservice.entity.LookupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ex.org.project.userservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String emailAddress);

    boolean existsByEmail(String emailAddress);

    List<User> findByStatus_NameAndStatus_Usage(String statusName, String statusUsage);

    List<User> findByStatusOrderByCreatedAtDesc(LookupStatus status);
}
