package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.dto.enums.BidStatus;

public record BidResponse(

        int bidSum,
        BidStatus status,
        int currentHighest,
        Boolean isAuto,
        Integer maxBidSum
) {
}


