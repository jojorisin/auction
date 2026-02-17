package se.jensen.johanna.auctionsite.dto;

import jakarta.validation.constraints.Positive;

public record BidRequest(
        @Positive
        int amount) {
}





