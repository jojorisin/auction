package se.jensen.johanna.auctionsite.dto.admin;

import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.List;


public record UpdateItemRequest(
        Category category,
        Category.SubCategory subCategory,
        String title,
        String description,
        Integer valuation,
        List<String> imageUrls) {


}
