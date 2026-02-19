package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record LaunchBatchRequest(
        @Positive
        Integer size,

        @Future
        Instant startTime,

        @Future
        Instant endTime
) {
    public LaunchBatchRequest {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
    }

    public Integer getSize() {
        return size != null ? size : 100;
    }

    public Instant getStartTime() {
        return startTime != null ? startTime : Instant.now();
    }

    public Instant getEndTime() {
        return endTime != null ? endTime : getStartTime().plus(7, ChronoUnit.DAYS);
    }
}
