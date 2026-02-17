package se.jensen.johanna.auctionsite.dto.admin;

import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;


public record UpdateAuctionRequest(
        AuctionStatus status,
        Integer acceptedPrice,
        Long itemId
) {


}
