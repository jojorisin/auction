package se.jensen.johanna.auctionsite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BidResponse;
import se.jensen.johanna.auctionsite.dto.enums.BidStatus;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.BidRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;
import se.jensen.johanna.auctionsite.util.TestDataFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {
    @InjectMocks
    private BidService bidService;

    @Mock
    private BidRepository bidRepository;
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private UserRepository userRepository;

    private static final Long BIDDER_ID = 1L;
    private static final Long ITEM_ID = 3L;
    private static final Long AUCTION_ID = 4L;


    private User bidder;
    private Auction auction;

    @BeforeEach
    void setUp() {

        bidder = TestDataFactory.createUser(BIDDER_ID);
        Item item = TestDataFactory.createItem(ITEM_ID, 1000);
        auction = TestDataFactory.createActiveAuction(AUCTION_ID, item);

    }


    @Test
    void placeFirstBid() {
        when(auctionRepository.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(bidder));

        BidRequest request = new BidRequest(200);

        BidResponse bidResponse = bidService.placeBid(request, BIDDER_ID, AUCTION_ID);

        assertEquals(100, bidResponse.bidSum());
        assertTrue(bidResponse.isAuto());
        assertEquals(BidStatus.LEADING, bidResponse.status());
        verify(bidRepository, times(1)).save(any(Bid.class));
    }
}