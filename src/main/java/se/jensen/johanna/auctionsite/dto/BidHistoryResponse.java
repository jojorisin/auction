package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;

public record BidHistoryResponse(
    Long userId,
    Long bidId,
    Integer bidSum,
    Instant createdAt,
    Boolean isAuto,
    Integer bidderAlias
) {

}
