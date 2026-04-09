package ex.org.project.userservice.exception;

public class SupportRequestTypeNotFoundException extends RuntimeException {

	public SupportRequestTypeNotFoundException(String message) {
        super(message);
    }
}
