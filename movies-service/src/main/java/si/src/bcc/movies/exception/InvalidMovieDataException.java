package si.src.bcc.movies.exception;

public class InvalidMovieDataException extends RuntimeException {
    public InvalidMovieDataException(String message) {
        super(message);
    }

    public InvalidMovieDataException(String field, String reason) {
        super("Invalid movie data: " + field + " - " + reason);
    }
}
