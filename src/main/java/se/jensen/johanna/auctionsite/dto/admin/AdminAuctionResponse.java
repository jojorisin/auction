package se.jensen.johanna.auctionsite.dto.admin;

import java.time.Instant;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;

public record AdminAuctionResponse(
    Long auctionId,
    AdminItemResponse adminItemResponse,
    Long buyerId,
    AuctionStatus status,
    int acceptedPrice,
    Instant startTime,
    Instant endTime) {

}
