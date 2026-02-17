package se.jensen.johanna.auctionsite.dto;

public record RefreshResult(
        String accessToken,
        String refreshToken
) {
}
