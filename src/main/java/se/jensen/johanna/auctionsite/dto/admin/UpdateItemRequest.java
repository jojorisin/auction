package se.jensen.johanna.auctionsite.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.List;

public record UpdateItemRequest(
        @Schema(implementation = Category.class, description = "Category of item", example = "ART")
        Category category,

        @Schema(implementation = Category.SubCategory.class, description = "Subcategory of item", example = "PAINTINGS")
        Category.SubCategory subCategory,

        @Size(min = 1, message = "Title is required if provided.")
        String title,

        @Size(min = 1, message = "Description is required if provided.")
        String description,

        @Positive(message = "Valuation must be a positive number.")
        Integer valuation,

        List<@NotBlank String> imageUrls,

        @Size(min = 1, message = "Image url is required if provided.")
        String imageUrl) {

    public UpdateItemRequest {
        if ((category != null && subCategory == null) || (category == null && subCategory != null)) {
            throw new IllegalArgumentException("Category and subcategory must be provided together.");
        }
        if (subCategory != null && !subCategory.getCategory().equals(category)) {
            throw new IllegalArgumentException("Subcategory must belong to category");
        }
    }
}
