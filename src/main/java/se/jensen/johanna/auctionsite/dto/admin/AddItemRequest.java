package se.jensen.johanna.auctionsite.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.List;

public record AddItemRequest(
        @NotNull(message = "Seller id is required")
        Long sellerId,

        @Schema(implementation = Category.class, description = "Category of item", example = "ART")
        @NotNull(message = "Please enter a category")
        Category category,

        @Schema(implementation = Category.SubCategory.class, description = "Subcategory of item", example = "ART_FINE_ART")
        @NotNull(message = "Please enter a subcategory")
        Category.SubCategory subCategory,

        @NotNull(message = "Please enter title")
        String title,

        @NotNull(message = "Please enter a description of object")
        String description,

        @NotNull(message = "Please enter a valuation")
        Integer valuation,

        List<String> imageUrls
) {
}
