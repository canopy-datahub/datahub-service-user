package ex.org.project.userservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ex.org.project.userservice.entity.LookupStatus;
import ex.org.project.userservice.entity.SupportRequest;

@Repository
public interface SupportRequestRepository  extends JpaRepository<SupportRequest, Integer> {
    
	List<SupportRequest> findByStatusUsageOrderByCreatedAtDesc(String statusUsage);

    List<SupportRequest> findByStatusOrderByCreatedAtDesc(LookupStatus status);

}
