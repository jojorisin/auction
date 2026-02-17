package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;

/**
 * @param status
 * @param error
 * @param message
 * @param timestamp
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
