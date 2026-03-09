package se.jensen.johanna.auctionsite.dto.my;

import java.util.List;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;

public record MyItemResponse(
    Long sellerId,
    Category category,
    Category.SubCategory subCategory,
    String title,
    String description,
    List<String> imageUrls,
    Integer valuation,
    ItemStatus status

) {

}
