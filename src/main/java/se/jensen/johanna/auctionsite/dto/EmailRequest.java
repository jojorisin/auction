package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.dto.enums.EmailType;

/**
 * @param type
 * @param email
 * @param auctionId
 * @param imageUrl
 * @param title
 */
public record EmailRequest(
        EmailType type,
        String email,
        Long auctionId,
        String imageUrl,
        String title
) {
}
