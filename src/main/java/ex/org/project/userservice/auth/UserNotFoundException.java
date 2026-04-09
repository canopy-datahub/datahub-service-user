package ex.org.project.userservice.auth;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) { super(message); }

}
