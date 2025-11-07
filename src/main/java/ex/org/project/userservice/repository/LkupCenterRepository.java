package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LkupCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LkupCenterRepository extends JpaRepository<LkupCenter, Integer> {

    Optional<LkupCenter> findByName(String name);

}
