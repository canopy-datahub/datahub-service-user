package ex.org.project.userservice.exception;

public class RasException extends RuntimeException{

    public RasException(String message){ super(message); }

    public RasException(String message, Throwable e){ super(message, e); }

}
