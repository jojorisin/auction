package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.EmailRequest;
import se.jensen.johanna.auctionsite.dto.OrderRequest;
import se.jensen.johanna.auctionsite.dto.enums.EmailType;
import se.jensen.johanna.auctionsite.exception.DomainStateException;
import se.jensen.johanna.auctionsite.model.AppUser;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;

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
    AppUser seller = auction.getItem().getSeller();

    if (status == AuctionStatus.SOLD) {
      Bid winningBid = auction.getWinningBid().orElseThrow(() ->
          new DomainStateException(String.format(
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
    if (bids.isEmpty()) {
      return;
    }
    Map<String, Bid> biddersToNotify = new HashMap<>();
    for (Bid b : bids) {
      biddersToNotify.putIfAbsent(b.getBidder().getEmail(), b);
    }
    for (Bid b : biddersToNotify.values()) {
      EmailType type =
          winningBid != null && b.getBidder().getId().equals(winningBid.getBidder().getId())
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

  private void notifySeller(Auction auction, AppUser seller) {
    emailService.sendEmail(new EmailRequest(
        auction.getStatus() == AuctionStatus.SOLD ? EmailType.ITEM_SOLD : EmailType.ITEM_NOT_SOLD,
        seller.getEmail(),
        auction.getId(),
        auction.getItem().getPrimaryImageUrl(),
        auction.getItem().getTitle()
    ));
  }
}
