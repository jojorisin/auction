package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.MaxBid;

/**
 * Returns result from bidding
 *
 * @param newBidderLeads True if the new bidder leads
 * @param newBid         The bid that's been put
 * @param otherBid       new bid for other if other maxbid was activated
 * @param isAuto         True if the new bid was a maxbid
 * @param maxBid         The maxbid for new bidder (if made)
 */
public record BiddingResult(
        boolean newBidderLeads,
        Bid newBid,
        Bid otherBid,
        boolean isAuto,
        MaxBid maxBid

) {
}
