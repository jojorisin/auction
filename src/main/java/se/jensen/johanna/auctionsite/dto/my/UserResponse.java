package se.jensen.johanna.auctionsite.dto.my;

public record UserResponse(
        Long userId,
        String email,
        String phoneNr,
        AddressResponse address

) {
}
