package si.src.bcc.actors.exception;

public class ActorNotFoundException extends RuntimeException {
    public ActorNotFoundException(Long id) {
        super("No actor found with id: " + id);
    }
}
