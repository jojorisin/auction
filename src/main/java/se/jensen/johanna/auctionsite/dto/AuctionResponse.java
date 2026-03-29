package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;

/**
 * For showing one specific auction
 *
 * @param itemResponse  DTO with item-details
 * @param auctionId     ID of auction
 * @param acceptedPrice accepted price for auction
 * @param endTime       end time
 * @param increment     the min amount to raise
 */
public record AuctionResponse(
    Long auctionId,
    int acceptedPrice,
    Instant endTime,
    int increment,
    ItemResponse itemResponse) {

}
