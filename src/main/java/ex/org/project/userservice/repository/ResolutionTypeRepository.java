package ex.org.project.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ex.org.project.userservice.entity.ResolutionType;

import java.util.Optional;

public interface ResolutionTypeRepository extends JpaRepository<ResolutionType, Long> {
    
	Optional<ResolutionType> findByName(String name);

}

