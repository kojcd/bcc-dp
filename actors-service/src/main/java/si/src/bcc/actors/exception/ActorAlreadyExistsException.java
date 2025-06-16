package si.src.bcc.actors.exception;

public class ActorAlreadyExistsException extends RuntimeException {
    public ActorAlreadyExistsException(String firstName, String lastName) {
        super("Actor already exists with name: " + firstName + " " + lastName);
    }
}
