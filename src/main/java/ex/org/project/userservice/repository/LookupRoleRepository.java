package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LookupRoleRepository extends JpaRepository<Role,Long> {
    List<Role> findAllByNameIn(List<String> roles);

}
