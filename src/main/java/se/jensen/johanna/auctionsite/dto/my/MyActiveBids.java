package se.jensen.johanna.auctionsite.dto.my;

import se.jensen.johanna.auctionsite.dto.enums.BidStatus;

import java.time.Instant;
import java.util.List;

/**
 * @param auctionId
 * @param title
 * @param imageUrls
 * @param endTime
 * @param status
 * @param highestBid
 * @param maxSum
 */
public record MyActiveBids(
        Long auctionId,
        String title,
        List<String> imageUrls,
        Instant endTime,
        BidStatus status,
        Integer highestBid,
        Integer maxSum

) {
}
