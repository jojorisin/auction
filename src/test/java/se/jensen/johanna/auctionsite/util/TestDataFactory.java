package se.jensen.johanna.auctionsite.util;

import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {
    public static User createUser(Long id) {
        return User.builder()
                   .id(id)
                   .email("test@test.com")
                   .role(Role.MEMBER)
                   .hashedPassword("<PASSWORD>")
                   .build();
    }

    public static Item createItem(Long id, int valuation) {
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("imageUrl");
        return Item.builder()
                   .id(id)
                   .seller(createUser(50L))
                   .title("Picasso")
                   .description("painting")
                   .category(Category.ART)
                   .subCategory(Category.SubCategory.PAINTINGS)
                   .valuation(valuation)
                   .imageUrls(imageUrls)
                   .build();
    }

    public static Auction createActiveAuction(Long id, Item item, int acceptedPrice) {
        return Auction.builder()
                      .id(id)
                      .item(item)
                      .acceptedPrice(acceptedPrice)
                      .startTime(Instant.now())
                      .endTime(Instant.now().plus(5, ChronoUnit.MINUTES))
                      .status(AuctionStatus.ACTIVE)
                      .build();
    }

    public static Auction createAnyAuction(
            Long id,
            Item item,
            int acceptedPrice,
            Instant startTime,
            Instant endTime,
            AuctionStatus status
    ) {
        return Auction.builder()
                      .id(id)
                      .item(item)
                      .acceptedPrice(acceptedPrice)
                      .startTime(startTime)
                      .endTime(endTime)
                      .status(status)
                      .build();
    }
}
