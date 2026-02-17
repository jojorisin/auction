package se.jensen.johanna.auctionsite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPhoneNumberException extends DomainException {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}
