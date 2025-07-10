package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LkupReferrer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LkupReferrerRepository extends JpaRepository<LkupReferrer, Integer> {

    List<LkupReferrer> findAllByOrderByDisplayOrderAsc();
}
