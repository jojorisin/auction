package se.jensen.johanna.auctionsite.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BidResponse;
import se.jensen.johanna.auctionsite.dto.enums.BidStatus;
import se.jensen.johanna.auctionsite.event.BidPlacedEvent;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.BidRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;
import se.jensen.johanna.auctionsite.util.AuctionTestBase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest extends AuctionTestBase {
    @InjectMocks
    private BidService bidService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    @DisplayName("Should place and save normal bid and return BELOW ACCEPTED LEADING")
    void placeNormalBidUnderAcceptedLeading() {
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(userRepository.findById(any())).thenReturn(Optional.of(currentBidder));

        BidResponse response = bidService.placeBid(new BidRequest(normalBidAmount), BIDDER_ID, AUCTION_ID);

        assertThat(response.status()).isEqualTo(BidStatus.BELOW_ACCEPTED_LEADING);
        assertThat(response.bidSum()).isEqualTo(normalBidAmount);
        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should place maxbid and put bid and return LEADING")
    void placeMaxBidOverAcceptedLeading() {
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(userRepository.findById(any())).thenReturn(Optional.of(currentBidder));

        BidResponse response = bidService.placeBid(new BidRequest(overAcceptedAmount), BIDDER_ID, AUCTION_ID);

        assertThat(response.status()).isEqualTo(BidStatus.LEADING);
        assertThat(response.bidSum()).isEqualTo(auction.getAcceptedPrice());
        assertThat(response.maxBidSum()).isEqualTo(overAcceptedAmount);
        assertThat(auction.getMaxBids().size()).isEqualTo(1);
        assertThat(auction.getBids().size()).isEqualTo(1);
        assertThat(response.isAuto()).isTrue();
        verify(bidRepository, times(1)).save(any(Bid.class));
        verify(auctionRepository).save(any(Auction.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(BidPlacedEvent.class));
    }

    @Test
    @DisplayName("Should place two equal max bids and return OUTBID for last bidder -first wins")
    void shouldPlaceMaxBidAndReturnOutbid() {
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(userRepository.findById(any())).thenReturn(Optional.of(competingBidder));

        BidResponse competingResponse = bidService.placeBid(
                new BidRequest(overAcceptedAmount * 2),
                OTHER_BIDDER_ID,
                AUCTION_ID
        );
        assertThat(competingResponse.status()).isEqualTo(BidStatus.LEADING);
        assertThat(competingResponse.isAuto()).isTrue();
        assertThat(competingResponse.bidSum()).isEqualTo(auction.getAcceptedPrice());

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(userRepository.findById(any())).thenReturn(Optional.of(currentBidder));

        BidResponse response = bidService.placeBid(new BidRequest(overAcceptedAmount * 2), BIDDER_ID, AUCTION_ID);

        assertThat(response.status()).isEqualTo(BidStatus.OUTBID);
        assertThat(response.isAuto()).isTrue();
        assertThat(response.bidSum()).isEqualTo(overAcceptedAmount * 2);
        assertThat(response.maxBidSum()).isEqualTo(overAcceptedAmount * 2);
        assertThat(auction.getMaxBids().size()).isEqualTo(2);
        assertThat(auction.getBids().size()).isEqualTo(3);
        verify(bidRepository, times(3)).save(any(Bid.class));
        verify(auctionRepository, times(2)).save(any(Auction.class));
        verify(applicationEventPublisher, times(2)).publishEvent(any(BidPlacedEvent.class));
    }

    @Test
    @DisplayName("Should raise with max bid for leading bidder and return 0 bidsum and not put bid")
    void shouldRaiseWithMaxBidForLeadingBidder() {
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(userRepository.findById(any())).thenReturn(Optional.of(currentBidder));

        BidResponse response = bidService.placeBid(new BidRequest(acceptedAmount), BIDDER_ID, AUCTION_ID);

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(userRepository.findById(any())).thenReturn(Optional.of(currentBidder));

        BidResponse raisedResponse = bidService.placeBid(new BidRequest(acceptedAmount + 1), BIDDER_ID, AUCTION_ID);

        assertThat(raisedResponse.status()).isEqualTo(BidStatus.LEADING);
        assertThat(raisedResponse.bidSum()).isEqualTo(0);
        assertThat(raisedResponse.maxBidSum()).isEqualTo(acceptedAmount + 1);
        verify(auctionRepository, times(2)).save(any(Auction.class));
        verify(bidRepository, times(1)).save(any(Bid.class));
        verify(applicationEventPublisher, times(2)).publishEvent(any(BidPlacedEvent.class));
    }
}