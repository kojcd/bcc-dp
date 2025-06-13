package si.src.bcc.movies.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.src.bcc.movies.dto.MovieRequest;
import si.src.bcc.movies.dto.MovieResponse;
import si.src.bcc.movies.mapper.MovieMapper;
import si.src.bcc.movies.model.Movie;
import si.src.bcc.movies.service.MovieService;
import si.src.bcc.movies.service.impl.MovieServiceImpl;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movie Management", description = "APIs for managing movies")
public class MovieController {

    private final MovieService movieService;
    private final MovieServiceImpl movieServiceImpl;
    private final MovieMapper movieMapper;

    @Autowired
    public MovieController(MovieService movieService, MovieServiceImpl movieServiceImpl, MovieMapper movieMapper) {
        this.movieService = movieService;
        this.movieServiceImpl = movieServiceImpl;
        this.movieMapper = movieMapper;
    }

    @Operation(summary = "Get all movies", description = "Retrieves a paginated list of all movies")
    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(pageable);
        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Page<MovieResponse> response = movies.map(movieMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get movie by IMDB ID", description = "Retrieves a movie by its IMDB ID")
    @GetMapping("/{imdbId}")
    public ResponseEntity<MovieResponse> getMovieById(
            @Parameter(description = "Movie IMDB ID") @PathVariable String imdbId) {
        Movie movie = movieService.getMovieById(imdbId);
        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(movieMapper.toResponse(movie));
    }

    @Operation(summary = "Create new movie", description = "Creates a new movie with the provided details")
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Parameter(description = "Movie details") @Valid @RequestBody MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        Movie createdMovie = movieService.createMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieMapper.toResponse(createdMovie));
    }

    @Operation(summary = "Update movie", description = "Updates an existing movie's details")
    @PutMapping("/{imdbId}")
    public ResponseEntity<MovieResponse> updateMovie(
            @Parameter(description = "Movie IMDB ID") @PathVariable String imdbId,
            @Parameter(description = "Updated movie details") @Valid @RequestBody MovieRequest request) {
        Movie movie = movieService.getMovieById(imdbId);
        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        movieMapper.updateEntity(movie, request);
        Movie updatedMovie = movieService.updateMovie(imdbId, movie);
        return ResponseEntity.ok(movieMapper.toResponse(updatedMovie));
    }

    @Operation(summary = "Delete movie", description = "Deletes a movie by its IMDB ID")
    @DeleteMapping("/{imdbId}")
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "Movie IMDB ID") @PathVariable String imdbId) {
        boolean deleted = movieService.deleteMovie(imdbId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search movies", description = "Searches movies by title or description with pagination")
    @GetMapping("/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Page<Movie> movies = movieService.searchMovies(searchTerm, pageable);
        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Page<MovieResponse> response = movies.map(movieMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get request statistics", description = "Retrieves the total number of requests made to the service")
    @GetMapping("/stats/requests")
    public ResponseEntity<Long> getRequestCount() {
        return ResponseEntity.ok(movieServiceImpl.getRequestCount());
    }
}