package se.jensen.johanna.auctionsite.dto;

import java.time.Instant;

public record ManualLaunchRequest(
        Instant startTime,
        Instant endTime
) {
}
