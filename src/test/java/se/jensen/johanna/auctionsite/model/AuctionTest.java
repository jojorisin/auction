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
    int acceptedAmount;
    int overAcceptedAmount;

    private static final Long BIDDER_ID = 1L;
    private static final Long OTHER_BIDDER_ID = 2L;
    private static final Long ITEM_ID = 3L;
    private static final Long AUCTION_ID = 4L;

    @BeforeEach
    void setUp() {

        // the increment is 500 for a 10000 valuation
        item = TestDataFactory.createItem(ITEM_ID, 10000);
        auction = TestDataFactory.createActiveAuction(AUCTION_ID, item, 3000);
        currentBidder = TestDataFactory.createUser(BIDDER_ID);
        competingBidder = TestDataFactory.createUser(OTHER_BIDDER_ID);
        increment = BidTier.getBidIncrement(item.getValuation());
        normalBidAmount = increment;
        maxBidAmount = increment * 3;
        acceptedAmount = auction.getAcceptedPrice();
        overAcceptedAmount = acceptedAmount + increment;
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
    @DisplayName("Should create max bid and put bid at accepted price")
    void shouldCreateMaxBid_andPutBidAtAcceptedPrice() {
        BiddingResult result = auction.placeBid(currentBidder, overAcceptedAmount);
        assertThat(result.maxBid().getMaxSum()).isEqualTo(overAcceptedAmount);
        assertThat(result.newBid().getBidSum()).isEqualTo(auction.getAcceptedPrice());
        System.out.println(result.maxBid().getMaxSum());
        System.out.println(result.newBid().getBidSum());
    }

    @Test
    @DisplayName("when raising bid with current over accepted normal bid, should create new max bid and not put bid")
    void whenRaised_shouldCreateMaxBid_andNotPutBid_whenCurrentIsNormalBid() {
        BiddingResult firstOverAcceptedBid = auction.placeBid(competingBidder, acceptedAmount);
        BiddingResult firstResult = auction.placeBid(currentBidder, acceptedAmount + increment);
        BiddingResult raisedResult = auction.placeBid(currentBidder, acceptedAmount + increment + 1);

        assertThat(firstOverAcceptedBid.newBid().getBidSum()).isEqualTo(auction.getAcceptedPrice());
        assertThat(firstResult.isAuto()).isFalse();
        assertThat(raisedResult.maxBid()).isNotNull();
        assertThat(raisedResult.newBid()).isNull();
        assertThat(auction.getWinningBid().get()).isEqualTo(firstResult.newBid());
    }

    @Test
    @DisplayName("when raising bid with current over accepted max bid, should create a new max bid and not put bid ")
    void whenRaised_shouldCreateMaxBid_andNotPutBidWhenCurrentIsMaxBid() {
        BiddingResult firstResult = auction.placeBid(currentBidder, overAcceptedAmount);
        BiddingResult raisedResult = auction.placeBid(currentBidder, overAcceptedAmount + 1);

        assertThat(auction.getBids()).hasSize(1);
        assertThat(auction.getMaxBids()).hasSize(2);
        assertThat(raisedResult.maxBid()).isNotNull();
        assertThat(raisedResult.newBid()).isNull();
        assertThat(auction.getWinningBid().get()).isEqualTo(firstResult.newBid());
    }

    @Test
    @DisplayName("when raising bid with current under accepted bid, should create max bid and put max amount, even if still under accepted")
    void whenRaised_shouldCreateMaxBid_andPutBidAtAcceptedPrice_whenCurrentIsUnderAccepted() {
        BiddingResult firstResult = auction.placeBid(currentBidder, normalBidAmount);
        BiddingResult raisedResult = auction.placeBid(currentBidder, acceptedAmount - 1);

        assertThat(auction.getBids()).hasSize(2);
        assertThat(raisedResult.newBid().getBidSum()).isEqualTo(acceptedAmount - 1);
        assertThat(raisedResult.maxBid().getMaxSum()).isEqualTo(acceptedAmount - 1);
    }

    @Test
    @DisplayName("Should generate bid from first max bid when triggered")
    void shouldActivateHiddenMaxBid_AndGenerateBidForHiddenMax() {
        // the first bidder bids a very high max bid
        BiddingResult result1 = auction.placeBid(competingBidder, acceptedAmount * 2);

        //bid sum to put from max is accepted price
        assertThat(result1.newBid().getBidSum()).isEqualTo(acceptedAmount);
        assertThat(auction.getBids()).hasSize(1);
        assertThat(auction.getMaxBids()).hasSize(1);

        // bidder is raising with min next bid
        BiddingResult result2 = auction.placeBid(currentBidder, auction.minNextBid());

        assertThat(auction.getBids()).hasSize(3);
        assertThat(auction.getMaxBids()).hasSize(1);
        assertThat(result2.otherBid().getBidder()).isEqualTo(competingBidder);
        assertThat(auction.getWinningBid().get().getBidder()).isEqualTo(competingBidder);
        assertThat(result2.newBidderLeads()).isFalse();
    }

    @Test
    @DisplayName("First bid should win when competing max bid have the same amount")
    void firstBidShouldWinWhenAmountIsTheSame() {
        auction.placeBid(competingBidder, overAcceptedAmount * 2);

        BiddingResult result2 = auction.placeBid(currentBidder, overAcceptedAmount * 2);

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