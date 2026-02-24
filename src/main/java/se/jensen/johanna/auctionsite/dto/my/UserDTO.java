package se.jensen.johanna.auctionsite.dto.my;

public record UserDTO(
        Long userId,
        String email,
        String phoneNr,
        AddressResponse address

) {
}
