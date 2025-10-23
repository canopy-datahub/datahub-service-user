package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LookupStatus;
import ex.org.project.userservice.entity.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRequestRepository  extends JpaRepository<SupportRequest, Integer> {

	List<SupportRequest> findByStatusUsageOrderByCreatedAtDesc(String statusUsage);

    List<SupportRequest> findByStatusOrderByCreatedAtDesc(LookupStatus status);

}
