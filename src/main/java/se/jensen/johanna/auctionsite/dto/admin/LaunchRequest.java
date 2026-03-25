package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;

public record LaunchRequest(
    @Positive
    @Max(value = 1000, message = "Size must be between 1 and 1000")
    Integer size,

    @FutureOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    Instant startTime,

    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    Instant endTime
) {

}
