package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.jcip.annotations.Immutable;

/**
 * Represents a Bid Entity in the auction system
 */
@Entity
@Immutable
@Table(name = "bids")
@AttributeOverride(name = "id", column = @Column(name = "bid_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Bid extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "bid_sum", nullable = false, updatable = false)
    private Integer bidSum;

    @Builder.Default
    private Boolean isAuto = false;

    /**
     * Creates a new normal bid
     * maxBid is set to bidSum
     *
     * @param bidSum  amount to bid
     * @param bidder  Who is bidding
     * @param auction Auction that is being bid on
     * @return A new bid
     */
    public static Bid createBid(Auction auction, User bidder, int bidSum) {
        validateBid(auction, bidder, bidSum);
        return Bid.builder()
                  .auction(auction)
                  .bidder(bidder)
                  .bidSum(bidSum)
                  .isAuto(false)
                  .build();
    }

    public static Bid generateBidFromMaxBid(Auction auction, User bidder, int bidSum) {
        validateBid(auction, bidder, bidSum);
        return Bid.builder()
                  .auction(auction)
                  .bidder(bidder)
                  .bidSum(bidSum)
                  .isAuto(true)
                  .build();
    }

    public static void validateBid(Auction auction, User bidder, int bidSum) {
        if (auction == null) throw new IllegalArgumentException("Auction is required");
        if (bidder == null) throw new IllegalArgumentException("Bidder is required");
        if (bidSum <= 0) throw new IllegalArgumentException("BidSum must be greater than 0");
    }
}
