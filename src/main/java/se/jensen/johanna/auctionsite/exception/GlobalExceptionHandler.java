package se.jensen.johanna.auctionsite.exception;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.jensen.johanna.auctionsite.dto.ErrorResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
        return buildErrorResponse(e);


    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return buildErrorResponse(e);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLockingException(OptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Concurrent modification detected"));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errors.put(fieldName, message);

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);

    }

    public ResponseEntity<ErrorResponse> buildErrorResponse(Exception e) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        HttpStatus httpStatus = status != null ? status.value() : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(
                httpStatus.value(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Instant.now()
        ));

    }


}
