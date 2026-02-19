package se.jensen.johanna.auctionsite.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.exception.AuctionClosedException;
import se.jensen.johanna.auctionsite.exception.InvalidBidException;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.service.enums.BidTier;
import se.jensen.johanna.auctionsite.util.TestDataFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

class AuctionTest {
    private Auction auction;
    private User currentBidder;
    private Item item;
    private User competingBidder;
    int increment;
    int normalBidAmount;
    int maxBidAmount;

    private static final Long BIDDER_ID = 1L;
    private static final Long OTHER_BIDDER_ID = 2L;
    private static final Long ITEM_ID = 3L;
    private static final Long AUCTION_ID = 4L;

    @BeforeEach
    void setUp() {
        item = TestDataFactory.createItem(ITEM_ID, 10000);
        auction = TestDataFactory.createActiveAuction(AUCTION_ID, item);
        currentBidder = TestDataFactory.createUser(BIDDER_ID);
        competingBidder = TestDataFactory.createUser(OTHER_BIDDER_ID);
        increment = BidTier.getBidIncrement(item.getValuation());
        normalBidAmount = increment;
        maxBidAmount = increment * 3;
    }

    @Test
    @DisplayName("Should create a normal bid for first bidder")
    void shouldCreateNormalBid_andPutBid() {
        BiddingResult result = auction.placeBid(currentBidder, normalBidAmount);

        assertThat(result.newBid()).isNotNull();
        assertThat(result.maxBid()).isNull();
        assertThat(auction.getBids()).hasSize(1);
        assertThat(auction.getMaxBids()).isEmpty();
        assertThat(result.isAuto()).isFalse();
        assertThat(result.newBid().getBidSum()).isEqualTo(increment);
    }

    @Test
    @DisplayName("Should create max bid and put bid at increment when bid is first")
    void shouldCreateMaxBid_andPutBidAtIncrement() {
        BiddingResult result = auction.placeBid(currentBidder, maxBidAmount);
        assertThat(result.newBid().getBidSum()).isEqualTo(increment);
        assertThat(auction.getBids()).hasSize(1);
        assertThat(auction.getMaxBids()).hasSize(1);
        assertThat(result.maxBid()).isNotNull();
        assertThat(result.newBid()).isNotNull();
    }

    @Test
    @DisplayName("when raising bid with current normal bid, should create new max bid and not put bid")
    void whenRaised_shouldCreateMaxBid_andNotPutBid_whenCurrentIsNormalBid() {
        BiddingResult firstResult = auction.placeBid(currentBidder, normalBidAmount);
        BiddingResult raisedResult = auction.placeBid(currentBidder, maxBidAmount);

        assertThat(firstResult.isAuto()).isFalse();
        assertThat(raisedResult.maxBid()).isNotNull();
        assertThat(raisedResult.newBid()).isNull();
        assertThat(auction.getWinningBid().get()).isEqualTo(firstResult.newBid());
    }

    @Test
    @DisplayName("when raising bid with current max bid, should create a new max bid and not put bid ")
    void whenRaised_shouldCreateMaxBid_andNotPutBidWhenCurrentIsMaxBid() {
        BiddingResult firstResult = auction.placeBid(currentBidder, maxBidAmount);
        BiddingResult raisedResult = auction.placeBid(currentBidder, maxBidAmount + 1);

        assertThat(auction.getBids()).hasSize(1);
        assertThat(auction.getMaxBids()).hasSize(2);
        assertThat(raisedResult.maxBid()).isNotNull();
        assertThat(raisedResult.newBid()).isNull();
        assertThat(auction.getWinningBid().get()).isEqualTo(firstResult.newBid());
    }

    @Test
    @DisplayName("Should generate bid from first max bid when triggered")
    void shouldActivateHiddenMaxBid_AndGenerateBidForHiddenMax() {
        BiddingResult result1 = auction.placeBid(competingBidder, maxBidAmount);

        assertThat(auction.getBids()).hasSize(1);
        assertThat(auction.getMaxBids()).hasSize(1);

        int currentHighest = result1.newBid().getBidSum();
        BiddingResult result2 = auction.placeBid(currentBidder, currentHighest + increment);

        assertThat(auction.getBids()).hasSize(3);
        assertThat(auction.getMaxBids()).hasSize(1);
        assertThat(result2.otherBid().getBidder()).isEqualTo(competingBidder);
        assertThat(auction.getWinningBid().get().getBidder()).isEqualTo(competingBidder);
        assertThat(result2.newBidderLeads()).isFalse();
    }

    @Test
    @DisplayName("First bid should win when competing max bid have the same amount")
    void firstBidShouldWinWhenAmountIsTheSame() {
        auction.placeBid(competingBidder, maxBidAmount);

        BiddingResult result2 = auction.placeBid(currentBidder, maxBidAmount);

        assertThat(result2.newBidderLeads()).isFalse();
        assertThat(auction.getBids()).hasSize(3);
        assertThat(auction.getMaxBids()).hasSize(2);
        assertThat(auction.getWinningBid().get().getBidder()).isEqualTo(competingBidder);
    }

    @Test
    @DisplayName("Should Not throw invalid amount when leading user is raising bid")
    void shouldNotThrowInvalidAmountWhenLeadingUserIsRaisingBid() {
        auction.placeBid(currentBidder, normalBidAmount);
        assertThatNoException().isThrownBy(() -> auction.placeBid(currentBidder, normalBidAmount + 1));
    }

    @Test
    @DisplayName("Should throw when bid is too low and leader is not raising")
    void shouldThrowInvalidBidWhenBidIsTooLow() {
        assertThatThrownBy(() -> auction.placeBid(competingBidder, increment - 1))
                .isInstanceOf(InvalidBidException.class);
    }

    @Test
    @DisplayName("Should softclose when bid comes in within one minute before endtime")
    void shouldSoftCloseWhenBidIsOneMinBeforeEndTime() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Instant originalEndTime = now.plus(30, ChronoUnit.SECONDS);
        Auction auction2 = TestDataFactory.createAnyAuction(
                AUCTION_ID,
                item,
                1000,
                now.minus(1, ChronoUnit.HOURS),
                originalEndTime,
                AuctionStatus.ACTIVE
        );
        auction2.placeBid(currentBidder, normalBidAmount);

        Instant expectedTime = now.plus(1, ChronoUnit.MINUTES);

        assertThat(auction2.getEndTime()).isCloseTo(expectedTime, within(1, ChronoUnit.SECONDS));
        assertThat(auction2.getEndTime()).isAfter(originalEndTime);
    }

    @Test
    @DisplayName("Should throw AuctionClosedException when endtime has passed but Auction still active")
    void shouldThrowAuctionClosedWhenBidComesInAfterEndTime() {
        Instant now = Instant.now();
        Instant startTime = now.minus(30, ChronoUnit.SECONDS);
        Instant endTime = now.minus(5, ChronoUnit.SECONDS);

        Auction auction2 = TestDataFactory.createAnyAuction(
                AUCTION_ID,
                item,
                800,
                startTime,
                endTime,
                AuctionStatus.ACTIVE
        );
        assertThatThrownBy(() -> auction2.placeBid(
                currentBidder,
                normalBidAmount
        )).isInstanceOf(AuctionClosedException.class);
    }
}