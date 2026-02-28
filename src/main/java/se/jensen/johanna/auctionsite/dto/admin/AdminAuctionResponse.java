package se.jensen.johanna.auctionsite.dto.admin;

import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;

import java.time.Instant;

public record AdminAuctionResponse(
        AdminItemResponse adminItemResponse,
        Long auctionId,
        Long buyerId,
        AuctionStatus status,
        int acceptedPrice,
        Instant endTime) {
}
