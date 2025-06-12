package si.src.bcc.movies.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.src.bcc.movies.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, String> {

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Movie> searchMovies(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Movie> findAll(Pageable pageable);
}
