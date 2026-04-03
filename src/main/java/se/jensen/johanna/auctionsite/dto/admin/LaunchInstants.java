package se.jensen.johanna.auctionsite.dto.admin;

import java.time.Instant;

public record LaunchInstants(
    Instant startTime,
    Instant endTime
) {

}
