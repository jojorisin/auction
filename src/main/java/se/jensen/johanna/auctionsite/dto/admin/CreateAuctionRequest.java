package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Positive;

public record CreateAuctionRequest(
    @Positive(message = "Accepted price must be positive. Leave empty for default value which is 40% of items valuation.")
    Integer acceptedPrice
) {

}


