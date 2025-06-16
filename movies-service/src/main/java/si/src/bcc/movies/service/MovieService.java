package si.src.bcc.movies.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import si.src.bcc.movies.model.Movie;
import java.util.List;

public interface MovieService {
    Page<Movie> getAllMovies(Pageable pageable);
    List<Movie> getAllMovies();
    Movie getMovieById(String imdbId);
    Movie createMovie(Movie movie);
    Movie updateMovie(String imdbId, Movie movie);
    boolean deleteMovie(String imdbId);
    Page<Movie> searchMovies(String searchTerm, Pageable pageable);
    boolean existsByImdbID(String imdbID);
    long getRequestCount();
}