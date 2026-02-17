package se.jensen.johanna.auctionsite.dto.my;

import java.time.Instant;
import java.util.List;

public record MyWonAuctionDTO(
        Long auctionId,
        String title,
        List<String> imageUrls,
        Instant endTime,
        Integer highestBid
) {
}
