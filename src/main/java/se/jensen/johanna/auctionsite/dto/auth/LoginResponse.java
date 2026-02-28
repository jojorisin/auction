package se.jensen.johanna.auctionsite.dto.auth;

import se.jensen.johanna.auctionsite.model.enums.Role;

public record LoginResponse(
        String accessToken,
        Long userId,
        Role role,
        String email
) {
}
