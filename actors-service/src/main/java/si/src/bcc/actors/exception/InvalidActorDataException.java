package si.src.bcc.actors.exception;

public class InvalidActorDataException extends RuntimeException {
    public InvalidActorDataException(String message) {
        super(message);
    }

    public InvalidActorDataException(String field, String reason) {
        super("Invalid actor data: " + field + " - " + reason);
    }
}