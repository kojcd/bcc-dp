package si.src.bcc.movies.mapper;

import org.springframework.stereotype.Component;
import si.src.bcc.movies.dto.MovieRequest;
import si.src.bcc.movies.dto.MovieResponse;
import si.src.bcc.movies.model.Movie;

@Component
public class MovieMapper {

    public Movie toEntity(MovieRequest request) {
        Movie movie = new Movie();
        movie.setImdbId(request.getImdbId());
        movie.setTitle(request.getTitle());
        movie.setYear(request.getYear());
        movie.setDescription(request.getDescription());
        movie.setActors(request.getActors());
        movie.setPictures(request.getPictures());
        return movie;
    }

    public MovieResponse toResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setImdbId(movie.getImdbId());
        response.setTitle(movie.getTitle());
        response.setYear(movie.getYear());
        response.setDescription(movie.getDescription());
        response.setActors(movie.getActors());
        response.setPictures(movie.getPictures());
        response.setCreatedAt(movie.getCreatedAt());
        response.setUpdatedAt(movie.getUpdatedAt());
        return response;
    }

    public void updateEntity(Movie movie, MovieRequest request) {
        movie.setTitle(request.getTitle());
        movie.setYear(request.getYear());
        movie.setDescription(request.getDescription());
        movie.setActors(request.getActors());
        movie.setPictures(request.getPictures());
    }
}