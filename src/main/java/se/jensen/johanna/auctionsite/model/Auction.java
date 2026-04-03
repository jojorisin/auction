package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.exception.AuctionClosedException;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.exception.DomainStateException;
import se.jensen.johanna.auctionsite.exception.InvalidBidException;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;
import se.jensen.johanna.auctionsite.service.enums.BidTier;

@Entity
@Table(name = "auctions")
@AttributeOverride(name = "id", column = @Column(name = "auction_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Auction extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "winning_bid_id")
  private Bid winningBid;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private AuctionStatus status = AuctionStatus.INACTIVE;

  private Integer acceptedPrice;

  private Instant startTime;

  private Instant endTime;

  @Version
  private Long version;

  @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
  @OrderBy("bidSum DESC, id DESC")
  @Builder.Default
  private List<Bid> bids = new ArrayList<>();

  @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("maxSum DESC, id DESC")
  @Builder.Default
  private List<MaxBid> maxBids = new ArrayList<>();

  public Optional<Bid> getWinningBid() {
    return Optional.ofNullable(winningBid);
  }

  public BiddingResult placeBid(AppUser bidder, int amount) {
    Instant now = Instant.now();
    checkAuctionIsOpen();
    BiddingResult result;

    // only a leading bidder can raise a bid, it can be raised with any amount
    //  if over accepted - only max bid is created
    // if under accepted - bid is put to auction and raised immediately
    if (winningBid != null && winningBid.getBidder().getId().equals(bidder.getId())) {
      int limitToRaise = leadingMaxBid().map(MaxBid::getMaxSum).orElse(winningBid.getBidSum());
      if (amount <= limitToRaise) {
        throw new DomainArgumentException("Place a higher bid to raise your bid.");
      }
      result = handleRaisedBid(bidder, amount);
      if (result.newBid() != null) {
        winningBid = result.newBid();
        softClose(now);
        return result;
      }
      touch();
      return result;
    }

    checkNewBidSumIsValid(amount);
    result = hiddenMaxBidExists() ? (handleHiddenMaxBid(bidder, amount))
        : handleNormalBid(bidder, amount);
    winningBid = result.otherBid() == null
        ? result.newBid()
        : result.newBidderLeads() ? result.newBid() : result.otherBid();

    softClose(now);
    return result;
  }

  public BiddingResult handleNormalBid(AppUser bidder, int amount) {
    boolean isNewBidMaxBid = isNewBidMaxBid(amount);
    int minNextBid = minNextBid();
    int amountToPut = amount;
    MaxBid newMax = null;

    if (isNewBidMaxBid) {
      newMax = MaxBid.create(this, bidder, amount);
      maxBids.add(newMax);
      amountToPut = Math.min(Math.max(minNextBid, acceptedPrice), amount);
    }
    Bid bidToPut =
        isNewBidMaxBid ? Bid.generateBidFromMaxBid(this, bidder, amountToPut) : Bid.createBid(
            this,
            bidder,
            amountToPut
        );
    bids.add(bidToPut);
    return new BiddingResult(true, bidToPut, null, isNewBidMaxBid, newMax);
  }

  /**
   * Creates a higher max bid for the current leading bidder and creates a new bid to put if under
   * accepted
   *
   * @param bidder the leading bidder that is raising
   * @param amount the new max amount
   */
  private BiddingResult handleRaisedBid(AppUser bidder, int amount) {
    MaxBid maxBid = MaxBid.create(this, bidder, amount);
    maxBids.add(maxBid);
    boolean acceptedMet = leadingAmount() >= acceptedPrice;
    if (!acceptedMet) {
      int bidToPut = Math.min(Math.max(minNextBid(), acceptedPrice), amount);
      Bid newBid = Bid.generateBidFromMaxBid(this, bidder, bidToPut);
      bids.add(newBid);
      return new BiddingResult(true, newBid, null, true, maxBid);
    }
    return new BiddingResult(true, null, null, true, maxBid);
  }

  /**
   * Handles bidding if a hidden max bid is activated
   *
   * @param bidder the incoming bidder
   * @param amount incoming amount
   */
  private BiddingResult handleHiddenMaxBid(AppUser bidder, int amount) {
    MaxBid hiddenMax = leadingMaxBid().orElseThrow(
        () -> new DomainStateException("Expected maxBid does not exist"));

    boolean isNewBidMaxBid = isNewBidMaxBid(amount);
    boolean newBidLeads = amount > hiddenMax.getMaxSum();

    //If lost - it is maxed out. If won - max or min next over loser
    int bidSumForHiddenMax = newBidLeads ? hiddenMax.getMaxSum() : Math.min(
        hiddenMax.getMaxSum(),
        amount + bidIncrement()
    );

    // Creates bid generated from max bid.
    Bid generatedBidForHiddenMax = Bid.generateBidFromMaxBid(this, hiddenMax.getBidder(),
        bidSumForHiddenMax);

    MaxBid newMax = null;
    int amountToPut = amount;

    // if the new bid is a max bid, the amount to generate is either max or next step over loser
    if (isNewBidMaxBid) {
      newMax = MaxBid.create(this, bidder, amount);
      this.maxBids.add(newMax);
      amountToPut = newBidLeads ? Math.min(amount, hiddenMax.getMaxSum() + bidIncrement()) : amount;
    }

    Bid newBid =
        isNewBidMaxBid ? Bid.generateBidFromMaxBid(this, bidder, amountToPut) : Bid.createBid(
            this,
            bidder,
            amountToPut
        );
    bids.add(newBid);
    bids.add(generatedBidForHiddenMax);
    return new BiddingResult(newBidLeads, newBid, generatedBidForHiddenMax, isNewBidMaxBid, newMax);
  }

  /**
   * Returns enumerated bid-increment {@link BidTier}.
   *
   * @return The bid-increment for the item depending on valuation
   */
  private int bidIncrement() {
    return BidTier.getBidIncrement(this.item.getValuation());
  }

  public void checkAuctionIsOpen() {
    if (this.endTime.isBefore(Instant.now()) || !this.status.equals(AuctionStatus.ACTIVE)) {
      throw new AuctionClosedException("Auction is closed");
    }
  }

  public void checkNewBidSumIsValid(int bidSum) {
    if (bidSum < minNextBid()) {
      throw new InvalidBidException("Bid is too low. Please raise your bid to participate.");
    }
  }

  /**
   * Checks if the incoming bid is a max bid
   *
   * @param bidSum the incoming bid sum
   * @return true if the bid is a max bid
   */
  public boolean isNewBidMaxBid(int bidSum) {
    return bidSum > minNextBid();
  }

  /**
   * Prepares Auction for item.
   *
   * @param item          Item to auction
   * @param acceptedPrice Optional accepted price
   * @return Auction
   */
  public static Auction createAuction(@NonNull Item item, Integer acceptedPrice) {
    if (!item.isReadyForAuction()) {
      throw new DomainStateException(
          String.format("Item with id %d is missing required fields", item.getId()));
    }
    int defaultAcceptedPrice = (item.getValuation() * 40) / 100;
    acceptedPrice = acceptedPrice != null ? acceptedPrice : defaultAcceptedPrice;
    if (acceptedPrice > item.getValuation()) {
      throw new DomainArgumentException(String.format(
          "Accepted price can't be higher than the item's valuation of %d",
          item.getValuation()
      ));
    }
    item.updateStatus(ItemStatus.PREPARED);
    return Auction.builder().item(item).acceptedPrice(acceptedPrice).status(AuctionStatus.INACTIVE)
        .build();
  }

  public void updateAcceptedPrice(Integer acceptedPrice) {
    if (status == AuctionStatus.ACTIVE) {
      throw new DomainStateException("Accepted price can't be changed when auction is active.");
    }
    if (acceptedPrice > item.getValuation()) {
      throw new DomainArgumentException(String.format(
          "Accepted price can't be higher than the item's valuation of %d",
          item.getValuation()
      ));
    }
    this.acceptedPrice = acceptedPrice;
  }

  /**
   * Launches Auction for public.
   *
   * @param startTime When auction will launch
   * @param endTime   When auction will end. Default 1 week + 1 min interval between auctions
   */
  public void launchAuction(Instant startTime, Instant endTime) {
    if (!isReadyToLaunch()) {
      throw new DomainStateException("Auction is missing required fields to launch.");
    }

    Instant now = Instant.now();
    Instant buffer = now.minus(Duration.ofMinutes(5));
    Instant minEndTime = startTime.plus(24, ChronoUnit.HOURS);

    if (endTime.isBefore(minEndTime) || startTime.isBefore(buffer)) {
      throw new DomainArgumentException("Invalid start and endtimes.");
    }
    this.startTime = startTime;
    this.endTime = endTime;
    if (!startTime.isAfter(now)) {
      status = AuctionStatus.ACTIVE;
    } else {
      status = AuctionStatus.PLANNED;
    }
    item.updateStatus(ItemStatus.ACTIVE);
  }

  /**
   * Soft closes auction when a bid comes in within 1 minute from end time
   */
  public void softClose(Instant now) {
    if (now.isAfter(endTime.minus(1, ChronoUnit.MINUTES))) {
      endTime = now.plus(1, ChronoUnit.MINUTES);
    }
  }

  public int leadingAmount() {
    return winningBid != null ? winningBid.getBidSum() : 0;
  }

  public boolean hiddenMaxBidExists() {
    return this.maxBids.stream().anyMatch(m -> m.getMaxSum() > leadingAmount());
  }

  public int minNextBid() {
    return leadingAmount() + BidTier.getBidIncrement(this.item.getValuation());
  }

  public Optional<MaxBid> leadingMaxBid() {
    return maxBids.stream().max(Comparator.comparing(MaxBid::getMaxSum)
        .thenComparing(Comparator.comparing(MaxBid::getCreatedAt).reversed()));
  }

  public AuctionStatus close() {
    if (winningBid == null) {
      status = AuctionStatus.EXPIRED;
      item.updateStatus(ItemStatus.NOT_SOLD);
      return status;
    }
    if (winningBid.getBidSum() < acceptedPrice) {
      status = AuctionStatus.ACCEPTED_NOT_MET;
      item.updateStatus(ItemStatus.NOT_SOLD);
      return status;
    }
    status = AuctionStatus.SOLD;
    item.updateStatus(ItemStatus.SOLD);
    return status;
  }

  public boolean isReadyToLaunch() {
    return item != null && acceptedPrice != null && acceptedPrice >= 0
        && status.isAvailableToLaunch();
  }

  public boolean acceptedMet() {
    return acceptedPrice != null && leadingAmount() >= acceptedPrice;
  }

  public void touch() {
    this.onUpdate();
  }

}


