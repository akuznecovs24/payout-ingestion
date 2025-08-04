package intrum.payoutingestion.exception;

public class ServiceErrorException extends RuntimeException {

    public ServiceErrorException(String message) {
        super(message);
    }

    public ServiceErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
