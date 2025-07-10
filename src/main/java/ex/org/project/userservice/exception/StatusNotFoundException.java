package ex.org.project.userservice.exception;

public class StatusNotFoundException extends RuntimeException {

	public StatusNotFoundException(String message) {
        super(message);
    }
}
