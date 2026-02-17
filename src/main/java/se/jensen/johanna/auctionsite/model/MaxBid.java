package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "max_bids")
@AttributeOverride(name = "id", column = @Column(name = "max_bid_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MaxBid extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @NotNull
    @Column(name = "max_sum", nullable = false, updatable = false)
    private Integer maxSum;


    public static MaxBid create(Auction auction, User bidder, int maxSum) {
        if (auction == null) throw new IllegalArgumentException("Auction is required");
        if (bidder == null) throw new IllegalArgumentException("Bidder is required");
        if (maxSum <= 0) throw new IllegalArgumentException("MaxSum must be greater than 0");
        return MaxBid.builder().auction(auction).bidder(bidder).maxSum(maxSum).build();

    }
}
