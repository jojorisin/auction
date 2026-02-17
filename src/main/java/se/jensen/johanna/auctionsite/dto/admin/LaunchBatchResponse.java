package se.jensen.johanna.auctionsite.dto.admin;

import java.util.List;

/**
 * @param successful
 * @param failed
 * @param failedAuctions
 */
public record LaunchBatchResponse(
        int successful,
        int failed,
        List<FailedToLaunch> failedAuctions

) {
}
