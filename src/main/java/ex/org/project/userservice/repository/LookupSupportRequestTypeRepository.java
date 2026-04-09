package ex.org.project.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ex.org.project.userservice.entity.LookupSupportRequestType;

@Repository
public interface LookupSupportRequestTypeRepository extends JpaRepository <LookupSupportRequestType, Long>{
    List<LookupSupportRequestType> findAll();
    
    Optional<LookupSupportRequestType> findByName(String name);

}
