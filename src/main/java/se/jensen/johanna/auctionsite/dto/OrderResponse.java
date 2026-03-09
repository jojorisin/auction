package se.jensen.johanna.auctionsite.dto;

import java.math.BigDecimal;
import se.jensen.johanna.auctionsite.model.enums.OrderStatus;

public record OrderResponse(
    Long orderId,
    BigDecimal orderSum,
    OrderStatus status,
    Long auctionId,
    String title,
    String imageUrl

) {

}
