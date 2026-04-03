package se.jensen.johanna.auctionsite.dto.admin;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;

public record LaunchRequest(
    @Positive
    @Max(value = 1000, message = "Size must be between 1 and 1000")
    Integer size,

    @NotNull
    AuctionStatus status,

    @FutureOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    LocalTime startTime,

    @Future
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    LocalTime endTime
) {

  public LaunchRequest {
    if (!status.equals(AuctionStatus.INACTIVE) && !status.equals(AuctionStatus.ACCEPTED_NOT_MET) &&
        !status.equals(AuctionStatus.EXPIRED)) {
      throw new IllegalArgumentException("Selected status is not available to launch.");
    }
  }

}
