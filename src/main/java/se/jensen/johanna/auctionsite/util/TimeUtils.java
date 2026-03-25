package se.jensen.johanna.auctionsite.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class TimeUtils {

  private static final ZoneId SWEDEN_ZONE = ZoneId.of("Europe/Stockholm");

  /**
   * Converts Instant from the client
   *
   * @param instantInput
   * @return
   */
  public static Instant fromLocalToUtc(Instant instantInput) {
    if (instantInput == null) {
      return null;
    }

    return instantInput
        .atZone(ZoneId.of("UTC"))
        .withZoneSameLocal(SWEDEN_ZONE)
        .toInstant();
  }

  public static Instant toUtcInstant(LocalDate date, LocalTime time) {
    if (date == null || time == null) {
      return null;
    }

    return date.atTime(time)
        .atZone(SWEDEN_ZONE)
        .toInstant();
  }

}
