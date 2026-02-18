package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.List;

public record ItemDTO(
        Integer valuation,
        Category category,
        Category.SubCategory subCategory,
        String title,
        String description,
        List<String> imageUrls
) {
}
