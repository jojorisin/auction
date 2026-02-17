package se.jensen.johanna.auctionsite.dto.admin;

import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;

import java.time.Instant;

public record ManualLaunchResponse(
        Long auctionId,
        AdminItemDTO adminItemDTO,
        Integer acceptedPrice,
        Instant startTime,
        Instant endTime,
        AuctionStatus status

) {
}
