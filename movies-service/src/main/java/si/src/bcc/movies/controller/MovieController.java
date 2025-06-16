package si.src.bcc.movies.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.src.bcc.movies.dto.MovieRequest;
import si.src.bcc.movies.dto.MovieResponse;
import si.src.bcc.movies.exception.InvalidMovieDataException;
import si.src.bcc.movies.exception.MovieAlreadyExistsException;
import si.src.bcc.movies.exception.MovieNotFoundException;
import si.src.bcc.movies.exception.NoMoviesFoundException;
import si.src.bcc.movies.mapper.MovieMapper;
import si.src.bcc.movies.model.Movie;
import si.src.bcc.movies.service.MovieService;
import si.src.bcc.movies.service.impl.MovieServiceImpl;
import java.util.List;
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

    @Operation(summary = "Get all movies", description = "Retrieves a full list of all movies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movies retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<List<MovieResponse>> getAllMoviesWithoutPagination() {
        List<Movie> movies = movieService.getAllMovies();
        if (movies.isEmpty()) {
            throw new NoMoviesFoundException();
        }
        List<MovieResponse> response = movies.stream()
                .map(movieMapper::toResponse)
                .toList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get all movies with pagination support", description = "Retrieves a paginated list of all movies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movies retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paged")
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @ParameterObject Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(pageable);
        if (movies.isEmpty()) {
            throw new NoMoviesFoundException();
        }
        Page<MovieResponse> response = movies.map(movieMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get movie by IMDB ID", description = "Retrieves a movie by its IMDB ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movie retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{imdbId}")
    public ResponseEntity<MovieResponse> getMovieById(
            @Parameter(description = "Movie IMDB ID") @PathVariable String imdbId) {
        Movie movie = movieService.getMovieById(imdbId);
        if (movie == null) {
            throw new MovieNotFoundException(imdbId);
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(movieMapper.toResponse(movie));
    }

    @Operation(summary = "Create new movie", description = "Creates a new movie with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movie created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid movie data"),
            @ApiResponse(responseCode = "409", description = "Movie already exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Parameter(description = "Movie details") @Valid @RequestBody MovieRequest request) {
        if (movieService.existsByImdbID(request.getImdbId())) {
            throw new MovieAlreadyExistsException(request.getImdbId());
        }
        Movie movie = movieMapper.toEntity(request);
        Movie createdMovie = movieService.createMovie(movie);
        if (createdMovie == null) {
            throw new InvalidMovieDataException("Failed to create movie");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieMapper.toResponse(createdMovie));
    }

    @Operation(summary = "Update movie", description = "Updates an existing movie's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid movie data"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{imdbId}")
    public ResponseEntity<MovieResponse> updateMovie(
            @Parameter(description = "Movie IMDB ID") @PathVariable String imdbId,
            @Parameter(description = "Updated movie details") @Valid @RequestBody MovieRequest request) {
        Movie movie = movieService.getMovieById(imdbId);
        if (movie == null) {
            throw new MovieNotFoundException(imdbId);
        }
        movieMapper.updateEntity(movie, request);
        Movie updatedMovie = movieService.updateMovie(imdbId, movie);
        if (updatedMovie == null) {
            throw new InvalidMovieDataException("Failed to update movie");
        }
        return ResponseEntity.ok(movieMapper.toResponse(updatedMovie));
    }

    @Operation(summary = "Delete movie", description = "Deletes a movie by its IMDB ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{imdbId}")
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "Movie IMDB ID") @PathVariable String imdbId) {
        if (!movieService.existsByImdbID(imdbId)) {
            throw new MovieNotFoundException(imdbId);
        }
        boolean deleted = movieService.deleteMovie(imdbId);
        if (!deleted) {
            throw new InvalidMovieDataException("Failed to delete movie");
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search movies with pagination support", description = "Searches movies by title or description with searchTerm and pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @ParameterObject Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new InvalidMovieDataException("searchTerm", "SearchTerm cannot be empty");
        }
        Page<Movie> movies = movieService.searchMovies(searchTerm, pageable);
        if (movies.isEmpty()) {
            throw new NoMoviesFoundException();
        }
        Page<MovieResponse> response = movies.map(movieMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get request statistics", description = "Retrieves the total number of requests made to the service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/stats/requests")
    public ResponseEntity<Long> getRequestCount() {
        return ResponseEntity.ok(movieServiceImpl.getRequestCount());
    }
}