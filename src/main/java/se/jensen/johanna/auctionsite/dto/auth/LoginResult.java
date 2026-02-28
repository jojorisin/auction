package se.jensen.johanna.auctionsite.dto.auth;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}
