package se.jensen.johanna.auctionsite.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import se.jensen.johanna.auctionsite.dto.admin.LaunchInstants;
import se.jensen.johanna.auctionsite.dto.admin.LaunchRequest;

@Component
public class TimeUtils {

  private static final ZoneId SWEDEN_ZONE = ZoneId.of("Europe/Stockholm");

  public static Instant toUtcInstant(LocalDate date, LocalTime time) {
    if (date == null || time == null) {
      return null;
    }
    return date.atTime(time).atZone(SWEDEN_ZONE).toInstant();
  }


  public static LaunchInstants getLaunchInstants(LaunchRequest request) {
    LocalDate localStartDate = request.startDate() != null ? request.startDate() : LocalDate.now();
    LocalTime localStartTime = request.startTime() != null ? request.startTime() : LocalTime.now();
    LocalDate localEndDate =
        request.endDate() != null ? request.endDate() : localStartDate.plusDays(7);
    LocalTime localEndTime = request.endTime() != null ? request.endTime() : localStartTime;

    return new LaunchInstants(toUtcInstant(localStartDate, localStartTime),
        toUtcInstant(localEndDate, localEndTime));
  }

}
