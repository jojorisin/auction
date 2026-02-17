package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import lombok.*;

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
}
