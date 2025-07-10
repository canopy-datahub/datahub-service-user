package ex.org.project.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ex.org.project.userservice.entity.LkupDCC;

import java.util.Optional;

@Repository
public interface LkupDCCRepository extends JpaRepository<LkupDCC, Integer> {

    Optional<LkupDCC> findByName(String name);

}
