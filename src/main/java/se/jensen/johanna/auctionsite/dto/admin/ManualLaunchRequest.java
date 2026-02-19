package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Future;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ManualLaunchRequest(
        @Future
        Instant startTime,

        @Future
        Instant endTime
) {
    public ManualLaunchRequest {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
    }

    public Instant getStartTime() {
        return startTime != null ? startTime : Instant.now();
    }

    public Instant getEndTime() {
        return endTime != null ? endTime : getStartTime().plus(7, ChronoUnit.DAYS);
    }
}
