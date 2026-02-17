package se.jensen.johanna.auctionsite.dto.my;


public record AppUserDTO(
        Long userId,
        String email,
        String phoneNr,
        AddressResponse address

) {
}
