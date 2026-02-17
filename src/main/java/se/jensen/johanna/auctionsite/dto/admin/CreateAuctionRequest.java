package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAuctionRequest(
        @NotNull @NotBlank(message = "Item id can't be empty") Long itemId,
        @NotNull @Positive Integer acceptedPrice


) {


}


