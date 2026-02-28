package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.EmailRequest;
import se.jensen.johanna.auctionsite.dto.OrderRequest;
import se.jensen.johanna.auctionsite.dto.enums.EmailType;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionClosingService {
    private final AuctionRepository auctionRepository;
    private final OrderService orderService;
    private final EmailService emailService;

    public void closeAuction(Auction auction) {
        AuctionStatus status = auction.close();
        auctionRepository.save(auction);
        User seller = auction.getItem().getSeller();

        if (status == AuctionStatus.SOLD) {
            Bid winningBid = auction.getWinningBid().orElseThrow(() ->
                    new IllegalStateException(String.format(
                            "Auction %d is SOLD but has no winning bid",
                            auction.getId()
                    )));
            orderService.createOrder(new OrderRequest(
                    auction,
                    seller,
                    winningBid.getBidSum(),
                    winningBid.getBidder()
            ));
            notifyBidders(auction.getBids(), winningBid);
        } else {
            notifyBidders(auction.getBids(), null);
        }
        notifySeller(auction, seller);
    }

    private void notifyBidders(List<Bid> bids, Bid winningBid) {
        if (bids.isEmpty()) return;

        for (Bid b : bids) {
            EmailType type = winningBid != null && b.getId().equals(winningBid.getId())
                    ? EmailType.WINNER : EmailType.LOST;
            emailService.sendEmail(new EmailRequest(
                    type,
                    b.getBidder().getEmail(),
                    b.getAuction().getId(),
                    b.getAuction().getItem().getPrimaryImageUrl(),
                    b.getAuction().getItem().getTitle()
            ));
        }
    }

    private void notifySeller(Auction auction, User seller) {
        emailService.sendEmail(new EmailRequest(
                auction.getStatus() == AuctionStatus.SOLD ? EmailType.ITEM_SOLD : EmailType.ITEM_NOT_SOLD,
                seller.getEmail(),
                auction.getId(),
                auction.getItem().getPrimaryImageUrl(),
                auction.getItem().getTitle()

        ));
    }
}
