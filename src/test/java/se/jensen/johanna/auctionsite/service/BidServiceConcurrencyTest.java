package se.jensen.johanna.auctionsite.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.BidRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class BidServiceConcurrencyTest {
    @Autowired
    private BidService bidService;

    @MockitoBean
    private BidRepository bidRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuctionRepository auctionRepository;

    @Test
    void shouldRetryOnOptimisticLockingException() {
        Auction mockAuction = mock(Auction.class);
        User mockUser = mock(User.class);
        BiddingResult mockResult = mock(BiddingResult.class);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(mockAuction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(mockAuction.placeBid(any(), anyInt())).thenReturn(mockResult);
        when(mockResult.newBidderLeads()).thenReturn(true);
        when(mockResult.newBid()).thenReturn(null);
        when(mockResult.otherBid()).thenReturn(null);

        when(auctionRepository.save(any()))
                .thenThrow(new OptimisticLockingFailureException("conflict"))
                .thenThrow(new OptimisticLockingFailureException("conflict"))
                .thenReturn(mockAuction);

        BidRequest bidRequest = new BidRequest(1000);

        assertDoesNotThrow(() -> bidService.placeBid(bidRequest, 1L, 1L));
        verify(auctionRepository, times(3)).save(any());
    }
}
