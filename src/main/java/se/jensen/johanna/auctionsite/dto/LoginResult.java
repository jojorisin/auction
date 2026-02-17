package se.jensen.johanna.auctionsite.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}
