package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository  extends JpaRepository<UserRole, Integer> {

	List<UserRole> findByRole_Name(String roleName);

}
