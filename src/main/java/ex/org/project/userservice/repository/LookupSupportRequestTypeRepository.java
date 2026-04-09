package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LookupSupportRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupSupportRequestTypeRepository extends JpaRepository <LookupSupportRequestType, Long>{
    List<LookupSupportRequestType> findAll();

    Optional<LookupSupportRequestType> findByName(String name);

}
