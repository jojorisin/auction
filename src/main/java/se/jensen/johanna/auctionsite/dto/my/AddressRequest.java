package se.jensen.johanna.auctionsite.dto.my;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String co,
        @NotBlank String streetName,
        String streetName2,
        @NotBlank @Pattern(regexp = "\\d{5}") String postalCode,
        @NotBlank String city,
        @NotBlank String country
) {
}
