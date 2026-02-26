package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.dto.enums.BidStatus;

/**
 * Returns bid-status after placing a bid to the current bidder
 *
 * @param bidSum         amount that was put
 * @param status         {@link BidStatus} bid status
 * @param currentHighest current highest leading bid
 * @param isAuto         if the bid created a max bid
 * @param maxBidSum      optional max bid sum if put
 */
public record BidResponse(
        Integer bidSum,
        BidStatus status,
        int currentHighest,
        Boolean isAuto,
        Integer maxBidSum
) {
}


