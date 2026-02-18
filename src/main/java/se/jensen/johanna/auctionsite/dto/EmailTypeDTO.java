package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.dto.enums.BidStatus;

/**
 * @param email
 * @param status
 * @param auctionId
 * @param imageUrl
 * @param title
 */
public record EmailTypeDTO(
        String email,
        BidStatus status,
        Long auctionId,
        String imageUrl,
        String title
) {
}
