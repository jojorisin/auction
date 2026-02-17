package se.jensen.johanna.auctionsite.exception;

public class JwtAuthenticationException extends DomainException {
    public JwtAuthenticationException(String message) {
        super(message);
    }
}
