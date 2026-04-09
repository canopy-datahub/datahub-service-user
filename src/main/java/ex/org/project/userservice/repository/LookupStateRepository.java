package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LookupState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupStateRepository extends JpaRepository<LookupState, Integer> {

    List<LookupState> findAllByOrderByDisplayOrder();

    Optional<LookupState> findByName(String name);
}
