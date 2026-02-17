package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;


public record BidHistoryDTO(
        Integer bidSum,
        Instant createdAt,
        Boolean isAuto,
        Integer bidderAlias,
        boolean isMe
) {

}
