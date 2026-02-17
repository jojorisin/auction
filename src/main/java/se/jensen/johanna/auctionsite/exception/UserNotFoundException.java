package se.jensen.johanna.auctionsite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends DomainException {


    public UserNotFoundException() {
        super("User not found.");
    }
}
