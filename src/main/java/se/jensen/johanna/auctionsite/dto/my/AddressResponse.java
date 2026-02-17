package se.jensen.johanna.auctionsite.dto.my;

public record AddressResponse(
        String firstName,
        String lastName,
        String co,
        String streetName,
        String streetName2,
        String postalCode,
        String city,
        String country
) {
}
