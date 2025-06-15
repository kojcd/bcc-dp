package si.src.bcc.actors.exception;

public class ActorNotFoundException extends RuntimeException {
    public ActorNotFoundException(String message) {
        super(message);
    }

    public ActorNotFoundException(Long id) {
        super("No Actor found with id: " + id);
    }
}
