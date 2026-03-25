package se.jensen.johanna.auctionsite.dto.admin;

import java.util.List;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;

public record AdminItemResponse(
    Long itemId,
    ItemStatus status,
    Long sellerId,
    Category category,
    Category.SubCategory subCategory,
    String title,
    String description,
    Integer valuation,
    List<String> imageUrls
) {

}
