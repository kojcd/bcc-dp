package si.src.bcc.movies.exception;

public class NoMoviesFoundException extends RuntimeException {
    public NoMoviesFoundException() {
        super("No movies found.");
    }
}
