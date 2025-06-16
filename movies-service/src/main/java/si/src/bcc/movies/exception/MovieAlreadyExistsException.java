package si.src.bcc.movies.exception;

public class MovieAlreadyExistsException extends RuntimeException {
    public MovieAlreadyExistsException(String imdbId) {
        super("Movie already exists with imdbID: " + imdbId);
    }
}
