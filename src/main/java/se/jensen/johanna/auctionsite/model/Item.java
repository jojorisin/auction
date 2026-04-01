package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.exception.DomainStateException;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;

/**
 * Represents the item to auction
 */
@Entity
@Table(name = "items")
@AttributeOverride(name = "id", column = @Column(name = "item_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "seller_id", nullable = false)
  private AppUser seller;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Category category;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Category.SubCategory subCategory;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Builder.Default
  private List<String> imageUrls = new ArrayList<>();

  @Column(nullable = false)
  private Integer valuation;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ItemStatus status = ItemStatus.AVAILABLE;

  public static Item create(
      @NonNull
      AppUser seller,
      @NonNull
      Category category,
      @NonNull
      Category.SubCategory subCategory,
      @NonNull
      String title,
      @NonNull
      String description,
      @NonNull
      Integer valuation,
      List<String> imageUrls
  ) {

    if (!checkValidSub(category, subCategory)) {
      throw new DomainArgumentException(
          "Subcategory must belong to the same category as the item.");
    }
    if (title.isBlank()) {
      throw new DomainArgumentException("Title is required");
    }
    if (description.isBlank()) {
      throw new DomainArgumentException("Description is required");
    }
    if (valuation <= 0) {
      throw new DomainArgumentException("Valuation must be greater than 0");
    }

    return Item.builder()
        .seller(seller)
        .category(category)
        .subCategory(subCategory)
        .title(title)
        .status(ItemStatus.AVAILABLE)
        .description(description)
        .valuation(valuation)
        .imageUrls(imageUrls != null ? imageUrls : new ArrayList<>())
        .build();
  }

  public void updateCategories(@NonNull Category category,
      @NonNull Category.SubCategory subCategory) {
    if (!subCategory.getCategory().equals(category)) {
      throw new DomainArgumentException(
          "Subcategory must belong to the same category as the item.");
    }
    this.category = category;
    this.subCategory = subCategory;
  }

  public void updateTitle(@NonNull String title) {
    if (title.isBlank()) {
      throw new DomainArgumentException("Title is required");
    }
    this.title = title;
  }

  public void updateDescription(@NonNull String description) {
    if (description.isBlank()) {
      throw new DomainArgumentException("Description is required");
    }
    this.description = description;
  }

  public void updateValuation(@NonNull Integer valuation) {
    if (status == ItemStatus.ACTIVE) {
      throw new DomainStateException("Item is active at auction, valuation cannot be changed");
    }
    if (valuation <= 0) {
      throw new DomainArgumentException("Valuation must be greater than 0");
    }
    this.valuation = valuation;
  }

  public String getPrimaryImageUrl() {
    if (imageUrls.isEmpty()) {
      return null;
    }
    return imageUrls.get(0);
  }


  public void addImageUrls(@NonNull List<String> imageUrls) {
    if (imageUrls.isEmpty()) {
      throw new DomainArgumentException("Images are required");
    }
    this.imageUrls.addAll(imageUrls);
  }

  public boolean isReadyForAuction() {
    return seller != null
        && category != null
        && checkValidSub(category, subCategory)
        && (title != null && !title.isBlank())
        && (description != null && !description.isBlank())
        && (valuation != null && valuation > 0)
        && (imageUrls != null && !imageUrls.isEmpty())
        && status.equals(ItemStatus.AVAILABLE);
  }

  private static boolean checkValidSub(Category category, Category.SubCategory subCategory) {
    return subCategory != null && subCategory.getCategory().equals(category);
  }

  public void updateStatus(ItemStatus status) {
    this.status = status;
  }
}
