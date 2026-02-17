package se.jensen.johanna.auctionsite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordMisMatchException extends DomainException {
    public PasswordMisMatchException(String message) {
        super(message);
    }

}
