package se.jensen.johanna.auctionsite.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import se.jensen.johanna.auctionsite.model.enums.Category;

public record CreateItemRequest(
    @NotNull(message = "Seller id is required")
    Long sellerId,

    @Schema(implementation = Category.class, description = "Category of item", example = "ART")
    @NotNull(message = "Please enter a category")
    Category category,

    @Schema(implementation = Category.SubCategory.class, description = "Subcategory of item", example = "PAINTINGS")
    @NotNull(message = "Please enter a subcategory")
    Category.SubCategory subCategory,

    @NotBlank(message = "Please enter title")
    String title,

    @NotBlank(message = "Please enter a description of the item")
    String description,

    @NotNull(message = "Please enter a valuation")
    @Positive
    Integer valuation,

    List<MultipartFile> imageFiles
) {

}
