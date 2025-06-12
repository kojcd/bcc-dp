package si.src.bcc.movies.service.impl;

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
import jakarta.persistence.EntityNotFoundException;
import java.util.concurrent.atomic.AtomicLong;

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
    @Cacheable(value = "movies", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Movie> getAllMovies(Pageable pageable) {
        incrementRequestCounter();
        return movieRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "movie", key = "#imdbId")
    public Movie getMovieById(String imdbId) {
        incrementRequestCounter();
        return movieRepository.findById(imdbId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found with imdbId: " + imdbId));
    }

    @Override
    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public Movie createMovie(Movie movie) {
        incrementRequestCounter();
        return movieRepository.save(movie);
    }

    @Override
    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public Movie updateMovie(String imdbId, Movie movie) {
        incrementRequestCounter();
        if (!movieRepository.existsById(imdbId)) {
            throw new EntityNotFoundException("Movie not found with imdbId: " + imdbId);
        }
        movie.setImdbId(imdbId);
        return movieRepository.save(movie);
    }

    @Override
    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public void deleteMovie(String imdbId) {
        incrementRequestCounter();
        if (!movieRepository.existsById(imdbId)) {
            throw new EntityNotFoundException("Movie not found with imdbId: " + imdbId);
        }
        movieRepository.deleteById(imdbId);
    }

    @Override
    @Cacheable(value = "movieSearch", key = "#searchTerm + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Movie> searchMovies(String searchTerm, Pageable pageable) {
        incrementRequestCounter();
        return movieRepository.searchMovies(searchTerm, pageable);
    }

    private void incrementRequestCounter() {
        requestCounter.incrementAndGet();
    }

    public long getRequestCount() {
        return requestCounter.get();
    }
}