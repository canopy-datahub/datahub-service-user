package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.UserReferrer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReferrerRepository extends JpaRepository<UserReferrer, Integer> {}
