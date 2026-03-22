package se.jensen.johanna.auctionsite.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.model.enums.Category;

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

    List<String> imageUrls,
    
    List<MultipartFile> imageFiles
) {

  public UpdateItemRequest {
    if ((category != null && subCategory == null) || (category == null && subCategory != null)) {
      throw new DomainArgumentException("Category and subcategory must be provided together.");
    }
    if (subCategory != null && !subCategory.getCategory().equals(category)) {
      throw new DomainArgumentException("Subcategory must belong to category");
    }
  }
}
