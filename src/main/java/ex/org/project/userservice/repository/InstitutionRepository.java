package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Integer> {

    List<Institution> findByStatus_NameOrderByName(String statusName);

    Optional<Institution> findByName(String name);

    boolean existsByName(String name);

}
