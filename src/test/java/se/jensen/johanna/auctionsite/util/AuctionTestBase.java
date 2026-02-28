package se.jensen.johanna.auctionsite.util;

import org.junit.jupiter.api.BeforeEach;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.service.enums.BidTier;

public abstract class AuctionTestBase {
    protected static final Long BIDDER_ID = 1L;
    protected static final Long OTHER_BIDDER_ID = 2L;
    protected static final Long ITEM_ID = 3L;
    protected static final Long AUCTION_ID = 4L;

    protected Auction auction;
    protected User currentBidder;
    protected User competingBidder;
    protected Item item;

    protected int increment;
    protected int normalBidAmount;
    protected int maxBidAmount;
    protected int acceptedAmount;
    protected int overAcceptedAmount;

    @BeforeEach
    void setUp() {
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
}
