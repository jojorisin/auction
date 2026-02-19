package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Positive;

public record UpdateAuctionRequest(
        @Positive
        Integer acceptedPrice,

        @Positive
        Long itemId
) {
}
