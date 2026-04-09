package ex.org.project.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ex.org.project.userservice.entity.Institution;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Integer> {
    
    List<Institution> findByStatus_NameOrderByName(String statusName);

    Optional<Institution> findByName(String name);

    boolean existsByName(String name);

}
