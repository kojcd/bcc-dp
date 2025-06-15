package si.src.bcc.actors.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.src.bcc.actors.model.Actor;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    @Query("SELECT a FROM Actor a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Actor> searchActors(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Actor> findAll(Pageable pageable);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);
} 