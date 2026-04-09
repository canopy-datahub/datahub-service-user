package ex.org.project.userservice.exception;

public class BadDataException extends RuntimeException {
    public BadDataException(String message){ super(message); }
}
