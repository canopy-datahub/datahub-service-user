package ex.org.project.userservice.controller;

import ex.org.project.datahub.auth.exception.UserAuthenticationException;
import ex.org.project.datahub.auth.exception.UserAuthorizationException;
import ex.org.project.datahub.auth.exception.UserNotFoundException;
import ex.org.project.userservice.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URISyntaxException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAuthorizationException.class)
    ResponseEntity<ExceptionResponseDTO> handleAuthorizationException(UserAuthorizationException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.FORBIDDEN;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Unauthorized",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    ResponseEntity<ExceptionResponseDTO> handleAuthenticationException(UserAuthenticationException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Unauthenticated",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleUserNotFoundException(UserNotFoundException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "User Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn(validationErrors);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Invalid Request",
                status.value(),
                validationErrors
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(SupportRequestNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleSupportRequestNotFoundException(SupportRequestNotFoundException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Support Request Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(StatusNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleStatusNotFoundException(StatusNotFoundException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Status Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(SupportRequestTypeNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleSupportRequestTypeNotFoundException(SupportRequestTypeNotFoundException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Support Request Type Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserRegistrationFormException.class)
    ResponseEntity<ExceptionResponseDTO> handleUserRegistrationFormException(UserRegistrationFormException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "User Registration Form Exception",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ExceptionResponseDTO> handleBadRequestException(BadRequestException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Bad Request",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(InstitutionCreationException.class)
    ResponseEntity<ExceptionResponseDTO> handleInstitutionCreationException(InstitutionCreationException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Institution Creation Exception",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(RasException.class)
    ResponseEntity<ExceptionResponseDTO> handleRasException(RasException e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "RAS Exception",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(URISyntaxException.class)
    ResponseEntity<ExceptionResponseDTO> handleURISyntaxException(URISyntaxException e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Internal Server Error",
                status.value(),
                "An error has occurred during redirect. Please contact support if the issue persists."
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(SubmitterCenterException.class)
    ResponseEntity<ExceptionResponseDTO> handleSubmitterCenterException(SubmitterCenterException e) {
        log.warn(e.getMessage(), e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Submitter Center Exception",
                status.value(),
                String.format("A user with the role of Data Submitter must be aligned to a valid center. %s", e.getMessage())
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserInfoException.class)
    ResponseEntity<ExceptionResponseDTO> handleUserInfoException(UserInfoException e) {
        log.warn(e.getMessage(), e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "User Info Exception",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(BadDataException.class)
    ResponseEntity<ExceptionResponseDTO> handleBadDataException(BadDataException e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Bad Data Exception",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Malformed Request Parameter",
                status.value(),
                "Failed to parse the provided request parameter type"
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleMalformedRequestException(MalformedRequestException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Malformed Request Parameter",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ExceptionResponseDTO> handleRuntimeException(RuntimeException e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Internal Server Error",
                status.value(),
                "An unknown error has occurred. Please contact support if the issue persists."
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExceptionResponseDTO> handleException(Exception e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Internal Server Error",
                status.value(),
                "An unknown error has occurred. Please contact support if the issue persists."
        );
        return new ResponseEntity<>(responseDTO, status);
    }
}
