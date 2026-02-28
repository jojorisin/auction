package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "orders")
@AttributeOverride(name = "id", column = @Column(name = "order_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(name = "order_sum", nullable = false)
    private Integer orderSum;

    /**
     * Creates an order
     *
     * @param auction
     * @param seller
     * @param orderSum
     * @param buyer
     * @return
     */
    public static Order create(Auction auction, User seller, Integer orderSum, User buyer) {
        if (auction == null) throw new IllegalArgumentException("Auction is required");
        if (buyer == null) throw new IllegalArgumentException("Buyer is required at creation of order");
        if (seller == null) throw new IllegalArgumentException("Seller is required at creation of order");
        if (orderSum == null) throw new IllegalArgumentException("OrderSum is required");
        if (orderSum <= 0) throw new IllegalArgumentException("OrderSum must be greater than 0");
        if (!auction.getItem().getSeller().equals(seller))
            throw new IllegalStateException("Seller must match item's seller");
        return Order.builder()
                    .auction(auction)
                    .seller(seller)
                    .orderSum(orderSum)
                    .buyer(buyer)
                    .build();
    }
}
