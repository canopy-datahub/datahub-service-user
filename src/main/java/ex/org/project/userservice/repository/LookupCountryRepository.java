package ex.org.project.userservice.repository;

import ex.org.project.userservice.entity.LookupCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupCountryRepository extends JpaRepository<LookupCountry, Integer> {

    Optional<LookupCountry> findByName(String name);

    List<LookupCountry> findAllByOrderByDisplayOrder();
}
