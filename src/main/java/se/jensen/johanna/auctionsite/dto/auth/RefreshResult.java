package se.jensen.johanna.auctionsite.dto.auth;

public record RefreshResult(
        String accessToken,
        String refreshToken
) {
}
