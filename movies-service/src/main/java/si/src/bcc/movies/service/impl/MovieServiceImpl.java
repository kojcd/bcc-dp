package si.src.bcc.movies.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.src.bcc.movies.model.Movie;
import si.src.bcc.movies.repository.MovieRepository;
import si.src.bcc.movies.service.MovieService;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final AtomicLong requestCounter = new AtomicLong(0);

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    @Cacheable(value = "movies", key = "'all'")
    public List<Movie> getAllMovies() {
        log.debug("Fetching all movies");
        incrementRequestCounter();
        List<Movie> movies = movieRepository.findAll();
        log.debug("Found {} movies", movies.size());
        return movies;
    }

    @Override
    @Cacheable(value = "movies", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Movie> getAllMovies(Pageable pageable) {
        log.debug("Fetching all movies with page: {} and size: {}", pageable.getPageNumber(), pageable.getPageSize());
        incrementRequestCounter();
        Page<Movie> movies = movieRepository.findAll(pageable);
        log.debug("Found {} movies", movies.getTotalElements());
        return movies;
    }

    @Override
    @Cacheable(value = "movie", key = "#imdbId")
    public Movie getMovieById(String imdbId) {
        log.debug("Fetching movie with imdbId: {}", imdbId);
        incrementRequestCounter();
        Movie movie = movieRepository.findById(imdbId).orElse(null);
        if (movie != null) {
            log.debug("Found movie: {}", movie.getTitle());
        } else {
            log.debug("No movie found with imdbId: {}", imdbId);
        }
        return movie;
    }

    @Override
    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public Movie createMovie(Movie movie) {
        log.debug("Creating new movie: {}", movie.getTitle());
        incrementRequestCounter();
        Movie savedMovie = movieRepository.save(movie);
        log.info("Created movie with imdbId: {} and title: {}", savedMovie.getImdbId(), savedMovie.getTitle());
        return savedMovie;
    }

    @Override
    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public Movie updateMovie(String imdbId, Movie movie) {
        log.debug("Updating movie with imdbId: {}", imdbId);
        incrementRequestCounter();
        if (!movieRepository.existsById(imdbId)) {
            log.debug("No movie found with imdbId: {} for update", imdbId);
            return null;
        }
        movie.setImdbId(imdbId);
        Movie updatedMovie = movieRepository.save(movie);
        log.info("Updated movie with imdbId: {} and title: {}", updatedMovie.getImdbId(), updatedMovie.getTitle());
        return updatedMovie;
    }

    @Override
    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public boolean deleteMovie(String imdbId) {
        log.debug("Attempting to delete movie with imdbId: {}", imdbId);
        incrementRequestCounter();
        if (!movieRepository.existsById(imdbId)) {
            log.debug("No movie found with imdbId: {} for deletion", imdbId);
            return false;
        }
        movieRepository.deleteById(imdbId);
        log.info("Successfully deleted movie with imdbId: {}", imdbId);
        return true;
    }

    @Override
    @Cacheable(value = "movieSearch", key = "#searchTerm + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Movie> searchMovies(String searchTerm, Pageable pageable) {
        incrementRequestCounter();
        return movieRepository.searchMovies(searchTerm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByImdbID(String imdbID) {
        return movieRepository.existsById(imdbID);
    }

    private void incrementRequestCounter() {
        long count = requestCounter.incrementAndGet();
        log.debug("Request counter incremented to: {}", count);
    }

    public long getRequestCount() {
        log.debug("Request counter returned: {}", requestCounter.get());
        return requestCounter.get();
    }
}