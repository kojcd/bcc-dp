package si.src.bcc.movies.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import si.src.bcc.movies.model.Movie;

public interface MovieService {
    Page<Movie> getAllMovies(Pageable pageable);
    Movie getMovieById(String imdbId);
    Movie createMovie(Movie movie);
    Movie updateMovie(String imdbId, Movie movie);
    void deleteMovie(String imdbId);
    Page<Movie> searchMovies(String searchTerm, Pageable pageable);
}