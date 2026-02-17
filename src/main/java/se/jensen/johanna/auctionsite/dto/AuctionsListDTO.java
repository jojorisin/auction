package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;
import java.util.List;

/**
 * For showing a list of auctions
 *
 * @param auctionId  ID of auction
 * @param imageUrls  Images
 * @param title      Title for the item
 * @param endTime    End time for auction
 * @param valuation  Valuation of the item
 * @param highestBid Current highest bid
 */

public record AuctionsListDTO(
        Long auctionId,
        List<String> imageUrls,
        String title,
        Instant endTime,
        Integer valuation,
        Integer highestBid


) {
}
