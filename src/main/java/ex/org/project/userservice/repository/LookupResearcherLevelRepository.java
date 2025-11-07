package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LookupResearcherLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LookupResearcherLevelRepository extends JpaRepository<LookupResearcherLevel, Long> {

	Optional<LookupResearcherLevel> findByName(String name);

}
