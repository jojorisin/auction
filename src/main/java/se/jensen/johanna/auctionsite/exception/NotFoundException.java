package se.jensen.johanna.auctionsite.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super("Not found");
    }
}
