package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@AttributeOverride(name = "id", column = @Column(name = "item_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category.SubCategory subCategory;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private Integer valuation;


    public static Item create(
            User seller,
            Category category,
            Category.SubCategory subCategory,
            String title,
            String description,
            Integer valuation,
            List<String> imageUrls
    ) {
        if (category == null) throw new IllegalArgumentException("Category is required");
        if (subCategory == null) throw new IllegalArgumentException("SubCategory is required");
        if (!checkValidSub(category, subCategory))
            throw new IllegalArgumentException("Subcategory must belong to the same category as the item.");
        if (seller == null) throw new IllegalArgumentException("Seller is required");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required");
        if (valuation == null || valuation <= 0) throw new IllegalArgumentException("Valuation must be greater than 0");


        return Item.builder()
                   .seller(seller)
                   .category(category)
                   .subCategory(subCategory)
                   .title(title)
                   .description(description)
                   .valuation(valuation)
                   .imageUrls(imageUrls != null ? imageUrls : new ArrayList<>())
                   .build();


    }

    public void update(
            Category category,
            Category.SubCategory subCategory,
            String title,
            String description,
            Integer valuation,
            List<String> imageUrls
    ) {

        if (category != null) this.category = category;
        if (subCategory != null) this.subCategory = subCategory;
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null && !description.isEmpty()) this.description = description;
        if (valuation != null && valuation > 0) this.valuation = valuation;
        if (imageUrls != null) this.imageUrls = imageUrls;

    }

    public void updateCategories(Category category, Category.SubCategory subCategory) {
        if (category == null || subCategory == null) {
            throw new IllegalArgumentException("Category and Subcategory is required.");
        }
        if (!subCategory.getCategory().equals(category)) {
            throw new IllegalArgumentException("Subcategory must belong to the same category as the item.");

        }
        this.category = category;
        this.subCategory = subCategory;
    }

    public void updateTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        this.title = title;
    }

    public void updateDescription(String description) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required");
        this.description = description;
    }

    public void updateValuation(Integer valuation) {
        if (valuation == null || valuation <= 0) throw new IllegalArgumentException("Valuation must be greater than 0");
        this.valuation = valuation;
    }

    public void addImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) throw new IllegalArgumentException("Image url is required");
        imageUrls.add(imageUrl);

    }

    public void updateImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) throw new IllegalArgumentException("ImageUrls is required");
        this.imageUrls = imageUrls;
    }

    public boolean isReadyForAuction() {
        return seller != null
                && category != null
                && checkValidSub(category, subCategory)
                && (title != null && !title.isBlank())
                && (description != null && !description.isBlank())
                && (valuation != null && valuation > 0)
                && (imageUrls != null && !imageUrls.isEmpty());
    }

    private static boolean checkValidSub(Category category, Category.SubCategory subCategory) {
        return subCategory != null && subCategory.getCategory().equals(category);
    }


}
