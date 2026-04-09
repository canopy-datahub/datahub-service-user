package ex.org.project.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ex.org.project.userservice.entity.LookupStatus;
import org.springframework.stereotype.Repository;

@Repository
public interface LookupStatusRepository extends JpaRepository<LookupStatus, Integer> {

    Optional<LookupStatus> findByNameAndUsage(String name, String usage);

    Optional<LookupStatus> findLookupStatusByNameAndUsage(String name, String usage);
    
    List<LookupStatus> findByUsage(String usage);

    Optional<LookupStatus> findByName(String name);
}

