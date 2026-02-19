package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.dto.enums.BidStatus;

public record BidResponse(
        Integer bidSum,
        BidStatus status,
        int currentHighest,
        Boolean isAuto,
        Integer maxBidSum
) {
}


