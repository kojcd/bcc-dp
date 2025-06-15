package si.src.bcc.actors.exception;

public class NoActorsFoundException extends RuntimeException {
    public NoActorsFoundException(String message) {
        super(message);
    }

    public NoActorsFoundException() {
        super("No Actors found.");
    }
}