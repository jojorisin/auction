package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;

/**
 * Represents a Bid Entity in the auction system
 */
@Entity
@Table(name = "bids")
@Immutable
@AttributeOverride(name = "id", column = @Column(name = "bid_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Bid extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "auction_id", nullable = false)
  private Auction auction;

  @ManyToOne
  @JoinColumn(name = "bidder_id", nullable = false, updatable = false)
  private User bidder;

  @Column(name = "bid_sum", nullable = false, updatable = false)
  private Integer bidSum;

  @Builder.Default
  private Boolean isAuto = false;

  /**
   * Creates a new normal bid maxBid is set to bidSum
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
    if (auction == null) {
      throw new DomainArgumentException("Auction is required");
    }
    if (bidder == null) {
      throw new DomainArgumentException("Bidder is required");
    }
    if (bidSum <= 0) {
      throw new DomainArgumentException("BidSum must be greater than 0");
    }
  }
}
