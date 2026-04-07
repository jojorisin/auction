package se.jensen.johanna.auctionsite.exception;

import com.stripe.exception.StripeException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.jensen.johanna.auctionsite.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(StripeException.class)
  public ResponseEntity<ErrorResponse> handleStripe(StripeException e) {
    log.error("StripeException - {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(
        502, e.getClass().getSimpleName(), "Payment processing error", Instant.now()
    ));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
    log.error("Bad credentials exception - {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
        401, e.getClass().getSimpleName(), "Invalid credentials", Instant.now()
    ));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
    log.error("Domain exception - type: {}, message: {}", e.getClass().getSimpleName(),
        e.getMessage());
    return buildErrorResponse(e);
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, String>> handleOptimisticLockingException(
      OptimisticLockingFailureException e) {
    log.warn("OptimisticLockingFailureException", e);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("message", "Concurrent modification detected"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationError(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      errors.put("message", fieldError.getDefaultMessage());
    }
    log.warn("Validation failed - fields: {}", errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled exception - {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        new ErrorResponse(500, "InternalServerError", "An unexpected error occurred",
            Instant.now()));
  }

  public ResponseEntity<ErrorResponse> buildErrorResponse(Exception e) {
    ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(e.getClass(),
        ResponseStatus.class);
    HttpStatus httpStatus = status != null ? status.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(httpStatus).body(new ErrorResponse(
        httpStatus.value(),
        e.getClass().getSimpleName(),
        e.getMessage(),
        Instant.now()
    ));
  }
}
