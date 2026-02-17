package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;

/**
 * For showing one specific auction
 *
 * @param itemDTO
 * @param auctionId
 * @param acceptedPrice
 * @param endTime
 * @param minNextBid
 */
public record AuctionDTO(

        Long auctionId,
        int acceptedPrice,
        Instant endTime,
        int minNextBid,
        ItemDTO itemDTO) {


}
