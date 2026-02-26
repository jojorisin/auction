package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAuctionRequest(
        @NotNull
        @NotNull(message = "Item id can't be empty")
        Long itemId,

        @NotNull(message = "Accepted price is required.")
        @Positive
        Integer acceptedPrice
) {
}


