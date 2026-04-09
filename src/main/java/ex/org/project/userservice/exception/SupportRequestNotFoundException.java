package ex.org.project.userservice.exception;

public class SupportRequestNotFoundException extends RuntimeException {

	public SupportRequestNotFoundException(String message) {
        super(message);
    }
}
