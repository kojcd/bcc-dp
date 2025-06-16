package si.src.bcc.movies.exception;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(String imdbId) {
        super("No movie found with imdbID: " + imdbId);
    }
}
