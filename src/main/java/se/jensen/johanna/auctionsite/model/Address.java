package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
public class Address {

  private String firstName;

  private String lastName;

  private String co;

  private String streetName;

  private String streetName2;

  @Pattern(regexp = "\\d{5}")
  private String postalCode;

  private String city;

  private String country;

  public static Address create(
      String firstName,
      String lastName,
      String co,
      String streetName,
      String streetName2,
      String postalCode,
      String city,
      String country
  ) {
    if (firstName == null || lastName == null || streetName == null || postalCode == null
        || city == null || country == null) {
      throw new DomainArgumentException("Address is missing required fields.");
    }
    return Address.builder().firstName(firstName).lastName(lastName).co(co).streetName(streetName)
        .streetName2(streetName2).postalCode(postalCode).city(city).country(country).build();
  }
}
