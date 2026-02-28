package se.jensen.johanna.auctionsite.dto.my;

import java.time.Instant;
import java.util.List;

public record WonAuctionResponse(
        Long auctionId,
        String title,
        List<String> imageUrls,
        Instant endTime,
        Integer highestBid
) {
}
