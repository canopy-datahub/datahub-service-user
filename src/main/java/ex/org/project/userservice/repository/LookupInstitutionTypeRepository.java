package ex.org.project.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ex.org.project.userservice.entity.LookupInstitutionType;

import java.util.Optional;

@Repository
public interface LookupInstitutionTypeRepository extends JpaRepository<LookupInstitutionType, Long> {

    Optional<LookupInstitutionType> findByName(String name);

}
