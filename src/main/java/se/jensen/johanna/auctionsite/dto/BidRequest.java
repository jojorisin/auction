package se.jensen.johanna.auctionsite.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BidRequest(
    @Positive
    @Max(value = 10000000, message = "Amount must be between 1 and 10000000")
    @NotNull
    Integer amount) {

}





