package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;

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
  private AppUser bidder;

  @NotNull
  @Column(name = "max_sum", nullable = false, updatable = false)
  private Integer maxSum;

  public static MaxBid create(Auction auction, AppUser bidder, int maxSum) {
    if (auction == null) {
      throw new DomainArgumentException("Auction is required");
    }
    if (bidder == null) {
      throw new DomainArgumentException("Bidder is required");
    }
    if (maxSum <= 0) {
      throw new DomainArgumentException("MaxSum must be greater than 0");
    }
    return MaxBid.builder().auction(auction).bidder(bidder).maxSum(maxSum).build();
  }
}
