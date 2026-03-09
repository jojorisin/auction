package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;

public record BidHistoryResponse(
    Long userId,
    Integer bidSum,
    Instant createdAt,
    Boolean isAuto,
    Integer bidderAlias
) {

}
