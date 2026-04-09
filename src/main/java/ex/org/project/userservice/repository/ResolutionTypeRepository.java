package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.ResolutionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResolutionTypeRepository extends JpaRepository<ResolutionType, Long> {

	Optional<ResolutionType> findByName(String name);

}

