package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
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

    @NotNull
    @Column(name = "bid_sum", nullable = false, updatable = false)
    private Integer bidSum;

    private Boolean isAuto;

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
        return Bid.builder()
                  .auction(auction)
                  .bidder(bidder)
                  .bidSum(bidSum)
                  .isAuto(false)
                  .build();
    }

    public static Bid generateBidFromMaxBid(Auction auction, User bidder, int bidSum) {
        return Bid.builder()
                  .auction(auction)
                  .bidder(bidder)
                  .bidSum(bidSum)
                  .isAuto(true)
                  .build();
    }
}
