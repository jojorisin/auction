package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.exception.AuctionClosedException;
import se.jensen.johanna.auctionsite.exception.InvalidBidException;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.service.enums.BidTier;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "auctions")
@AttributeOverride(name = "id", column = @Column(name = "auction_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Auction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @OneToOne
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

    public BiddingResult placeBid(User bidder, int amount) {
        Instant now = Instant.now();
        checkAuctionIsOpen();

        BiddingResult result;

        // a raised bid is not put to auction immediately.
        // it does not trigger a softclose.
        // it can be raised with any amount.
        if (winningBid != null && winningBid.getBidder().getId().equals(bidder.getId())) {
            int limitToRaise = leadingMaxBid().map(MaxBid::getMaxSum).orElse(winningBid.getBidSum());
            if (amount <= limitToRaise) {
                throw new IllegalArgumentException("Place a higher bid to raise your bid.");
            }
            return handleRaisedBid(bidder, amount);
        }

        checkNewBidSumIsValid(amount);

        if (bids.isEmpty() && maxBids.isEmpty()) {
            result = handleFirstBid(bidder, amount);
        } else if (hiddenMaxBidExists()) {
            result = handleHiddenMaxBid(bidder, amount);
        } else {
            result = handleNormalOverbid(bidder, amount);
        }

        winningBid = result.otherBid() == null
                ? result.newBid()
                : result.newBidderLeads() ? result.newBid() : result.otherBid();

        softClose(now);

        return result;
    }

    /**
     * handles the first bid for auction
     */
    public BiddingResult handleFirstBid(User bidder, int amount) {
        int minNextBid = leadingAmount() + bidIncrement();
        boolean isNewBidMaxBid = isNewBidMaxBid(amount);
        int amountToPut = amount;
        MaxBid maxBid = null;

        if (isNewBidMaxBid) {
            maxBid = MaxBid.create(this, bidder, amount);
            maxBids.add(maxBid);
            amountToPut = minNextBid;
        }

        Bid newBid = isNewBidMaxBid ? Bid.generateBidFromMaxBid(this, bidder, amountToPut) : Bid.createBid(
                this,
                bidder,
                amountToPut
        );
        bids.add(newBid);
        return new BiddingResult(true, newBid, null, isNewBidMaxBid, maxBid);
    }

    /**
     * Creates a higher max bid for the current leading bidder
     *
     * @param bidder the leading bidder that is raising
     * @param amount the new max amount
     */
    private BiddingResult handleRaisedBid(User bidder, int amount) {
        MaxBid maxBid = MaxBid.create(this, bidder, amount);
        this.maxBids.add(maxBid);
        return new BiddingResult(true, null, null, true, maxBid);
    }

    /**
     * Handles bidding if a hidden max bid is activated
     *
     * @param bidder the incoming bidder
     * @param amount incoming amount
     */
    private BiddingResult handleHiddenMaxBid(User bidder, int amount) {
        MaxBid hiddenMax = leadingMaxBid().orElseThrow(() -> new IllegalStateException("Expected maxBid does not exist"));

        boolean isNewBidMaxBid = isNewBidMaxBid(amount);
        boolean newBidLeads = amount > hiddenMax.getMaxSum();

        //If lost - it is maxed out. If won - max or min next over loser
        int bidSumForHiddenMax = newBidLeads ? hiddenMax.getMaxSum() : Math.min(
                hiddenMax.getMaxSum(),
                amount + bidIncrement()
        );

        // Creates bid generated from max bid.
        Bid generatedBidForHiddenMax = Bid.generateBidFromMaxBid(this, hiddenMax.getBidder(), bidSumForHiddenMax);

        MaxBid newMax = null;
        int amountToPut = amount;

        // if the new bid is a max bid, the amount to generate is either max or next step over loser
        if (isNewBidMaxBid) {
            newMax = MaxBid.create(this, bidder, amount);
            this.maxBids.add(newMax);
            amountToPut = newBidLeads ? Math.min(amount, hiddenMax.getMaxSum() + bidIncrement()) : amount;
        }

        Bid newBid = isNewBidMaxBid ? Bid.generateBidFromMaxBid(this, bidder, amountToPut) : Bid.createBid(
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

    public BiddingResult handleNormalOverbid(User bidder, int amount) {
        boolean isNewBidMaxBid = isNewBidMaxBid(amount);
        int minNextBid = minNextBid();
        int amountToPut = amount;
        MaxBid newMax = null;

        if (isNewBidMaxBid) {
            newMax = MaxBid.create(this, bidder, amount);
            this.maxBids.add(newMax);
            amountToPut = minNextBid;
        }
        Bid bidToPut = isNewBidMaxBid ? Bid.generateBidFromMaxBid(this, bidder, amountToPut) : Bid.createBid(
                this,
                bidder,
                amountToPut
        );
        bids.add(bidToPut);
        return new BiddingResult(true, bidToPut, null, isNewBidMaxBid, newMax);
    }

    /**
     * Prepares Auction for item.
     *
     * @param item          Item to auction
     * @param acceptedPrice Optional accepted price
     * @return Auction
     */
    public static Auction prepareAuction(Item item, Integer acceptedPrice) {
        if (item == null || !item.isReadyForAuction()) throw new IllegalArgumentException("Item cannot be null");
        acceptedPrice = acceptedPrice == null ? 0 : acceptedPrice;
        return Auction.builder().item(item).acceptedPrice(acceptedPrice).status(AuctionStatus.INACTIVE).build();
    }

    public void updateItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required to update auction item.");
        }
        this.item = item;
    }

    public void updateAcceptedPrice(Integer acceptedPrice) {
        if (acceptedPrice == null || acceptedPrice < 0) {
            throw new IllegalArgumentException("Accepted price must be a positive number.");
        }
        this.acceptedPrice = acceptedPrice;
    }

    public void updateStatus(AuctionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.status = status;
    }

    /**
     * Launches Auction for public.
     *
     * @param startTime When auction will launch
     * @param endTime   When auction will end. Default 1 week + 1 min interval between auctions
     */
    public Auction launchAuction(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end time must be set.");
        }

        if (!isReadyToLaunch()) {
            throw new IllegalStateException("Auction is missing required fields to launch.");
        }

        Instant now = Instant.now();
        Instant buffer = now.minus(Duration.ofMinutes(1));
        Instant minEndTime = startTime.plus(24, ChronoUnit.HOURS);

        if (endTime.isBefore(startTime) || endTime.isBefore(minEndTime) || startTime.isBefore(buffer)) {
            throw new IllegalArgumentException("Invalid start and endtimes.");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        if (!startTime.isAfter(now)) {
            status = AuctionStatus.ACTIVE;
        } else {
            status = AuctionStatus.PLANNED;
        }
        return this;
    }

    /**
     * Soft closes auction when a bid comes in within 1 minute from end time
     */
    public void softClose(Instant now) {
        Duration buffer = Duration.ofMinutes(1);
        Instant softCloseThreshold = this.endTime.minus(buffer);
        if (now.isAfter(softCloseThreshold)) {
            this.endTime = now.plus(buffer);
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

    public void closeSoldAuction(Bid winningBid) {
        if (winningBid == null) {
            throw new IllegalArgumentException("Winning bid is required to close auction as SOLD.");
        }
        this.status = AuctionStatus.SOLD;
    }

    public void closeExpiredAuction() {
        this.status = AuctionStatus.EXPIRED;
    }

    public void closeAuctionAcceptedNotMet() {
        this.status = AuctionStatus.EXPIRED;
    }

    public boolean isReadyToLaunch() {
        return item != null && item.isReadyForAuction() && acceptedPrice != null && acceptedPrice >= 0 && status == AuctionStatus.INACTIVE;
    }
}


