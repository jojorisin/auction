package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.List;

/**
 * Contains details about the item at auction.
 * Primarily used in AuctionDTOs
 *
 * @param valuation
 * @param category
 * @param subCategory
 * @param title
 * @param description
 * @param imageUrls
 */
public record ItemDTO(
        Integer valuation,
        Category category,
        Category.SubCategory subCategory,
        String title,
        String description,
        List<String> imageUrls
) {
}
