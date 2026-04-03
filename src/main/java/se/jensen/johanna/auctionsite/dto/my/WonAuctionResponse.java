package se.jensen.johanna.auctionsite.dto.my;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import se.jensen.johanna.auctionsite.model.enums.OrderStatus;

public record WonAuctionResponse(
    Long auctionId,
    Long orderId,
    OrderStatus status,
    String title,
    List<String> imageUrls,
    Instant endTime,
    BigDecimal winningBid
) {

}
