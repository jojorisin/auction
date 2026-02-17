package se.jensen.johanna.auctionsite.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.exception.InvalidBidException;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.service.enums.BidTier;
import se.jensen.johanna.auctionsite.util.TestDataFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {
    private Auction auction;
    private User currentBidder;
    private Item item;
    private User competingBidder;
    int increment;

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

    }

    @Test
    @DisplayName("Handle first normal bid")
    void shouldCreateNormalBid_andPutBid() {
        int amount = 500;
        BiddingResult result = auction.placeBid(currentBidder, amount);

        assertTrue(result.newBidderLeads());
        assertFalse(result.isAuto());
        assertEquals(BidTier.getBidIncrement(item.getValuation()), result.newBid().getBidSum());
        assertNull(result.otherBid());

    }

    @Test
    @DisplayName("Handle first auto bid")
    void shouldCreateMaxBid_andPutBidAtIncrement() {
        int amount = 2000;
        BiddingResult result = auction.placeBid(currentBidder, amount);
        assertTrue(result.newBidderLeads());
        assertTrue(result.isAuto());
        assertEquals(increment, result.newBid().getBidSum());
        assertNull(result.otherBid());
        assertNotNull(result.newBid());
    }

    @Test
    @DisplayName("Handle raised bid")
    void shouldCreateMaxBid_andNotPutBid() {
        int firstAmount = 2000;
        int secondAmount = 2001;
        BiddingResult result = auction.placeBid(currentBidder, firstAmount);
        BiddingResult raisedResult = auction.placeBid(currentBidder, secondAmount);

        assertTrue(result.newBidderLeads());
        assertTrue(raisedResult.newBidderLeads());
        assertTrue(result.isAuto());
        assertTrue(raisedResult.isAuto());
        assertNotNull(result.newBid());
        assertEquals(increment, result.newBid().getBidSum());
        assertNull(raisedResult.newBid());
        assertNotNull(result.newBid());
        assertNull(result.otherBid());
        System.out.println(result + " " + raisedResult);
    }

    @Test
    @DisplayName("Handle hidden max bid")
    void shouldActivateHiddenMaxBid_AndGenerateBidForHiddenMax() {
        int otherBidderAmount = 3000;
        int newBidAmount = 2500;
        BiddingResult result1 = auction.placeBid(competingBidder, otherBidderAmount);
        assertEquals(1, auction.getBids().size());
        assertEquals(1, auction.getMaxBids().size());

        BiddingResult result2 = auction.placeBid(currentBidder, newBidAmount);
        assertEquals(3, auction.getBids().size());
        assertEquals(2, auction.getMaxBids().size());

        assertTrue(result1.newBidderLeads());
        assertTrue(result1.isAuto());
        assertEquals(increment, result1.newBid().getBidSum());
        assertFalse(result2.newBidderLeads());
        assertTrue(result2.isAuto());
        assertNotNull(result2.newBid());
        assertNotNull(result2.otherBid());

    }

    @Test
    @DisplayName("Should Not throw invalid amount when leading user is raising bid")
    void shouldNotThrowInvalidAmountWhenLeadingUserIsRaisingBid() {
        int amount = 500;
        auction.placeBid(currentBidder, amount);
        auction.placeBid(currentBidder, amount + 1);
    }

    @Test
    @DisplayName("Should throw when bid is too low and leader is not raising")
    void shouldThrowInvalidBidWhenBidIsTooLow() {
        int amount = increment - 1;
        assertThrows(InvalidBidException.class, () -> auction.placeBid(currentBidder, amount));
    }

    @Test
    @DisplayName("Should softclose when bid comes in within one minute before endtime")
    void shouldSoftCloseWhenBidIsOneMinBeforeEndTime() {
        int amount = 500;
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
        auction2.placeBid(currentBidder, amount);


        Instant expectedTime = now.plus(1, ChronoUnit.MINUTES);

        assertThat(auction2.getEndTime()).isCloseTo(expectedTime, within(1, ChronoUnit.SECONDS));
        assertTrue(auction2.getEndTime().isAfter(originalEndTime));

    }
}