package se.jensen.johanna.auctionsite.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BidRequest(
        @Positive
        @NotNull
        Integer amount) {
}





