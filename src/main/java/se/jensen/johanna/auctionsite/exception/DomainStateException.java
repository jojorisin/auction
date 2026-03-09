package se.jensen.johanna.auctionsite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DomainStateException extends DomainException {

  public DomainStateException(String message) {
    super(message);
  }
}
