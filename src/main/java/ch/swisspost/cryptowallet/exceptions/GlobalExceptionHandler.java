package ch.swisspost.cryptowallet.exceptions;

import ch.swisspost.cryptowallet.dtos.ResponseObject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * @author Oke
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CryptoWalletClientException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(CryptoWalletClientException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({CryptoWalletServerException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(CryptoWalletServerException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> errorHandler(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage())
                .build());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> errorHandler(ConstraintViolationException e) {
        log.error(e.getMessage(), e);
        String messages = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(",\n"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(messages)
                .build());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message("An error has occurred while processing your request, please try again")
                .build());
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(RuntimeException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message("An error has occurred while processing your request, please try again")
                .build());
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(EntityNotFoundException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(UsernameNotFoundException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(AccessDeniedException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ResponseObject<Object>> errorHandler(BadCredentialsException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(e.getMessage())
                .build());
    }

}
