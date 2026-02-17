package se.jensen.johanna.auctionsite.dto.admin;

import java.time.Instant;

public record AdminBidDTO(
        Long userId,
        Long auctionId,
        Integer bidSum,
        Integer maxBid,
        Instant autoTimestamp,
        Instant createdAt


) {
}
