package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public record LaunchRequestWeb(
    @Positive Integer size,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    LocalTime startTime,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    LocalTime endTime
) {

}