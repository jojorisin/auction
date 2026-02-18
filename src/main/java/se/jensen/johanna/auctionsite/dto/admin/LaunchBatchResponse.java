package se.jensen.johanna.auctionsite.dto.admin;

import java.util.List;

/**
 * @param successful     the number of launches that was successful
 * @param failed         the number of launches that failed
 * @param failedAuctions list of failed auctions with id of auction and message
 */
public record LaunchBatchResponse(
        int successful,
        int failed,
        List<FailedToLaunch> failedAuctions
) {
}
