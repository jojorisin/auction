package se.jensen.johanna.auctionsite.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.BidHistoryResponse;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BidResponse;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.dto.enums.BidStatus;
import se.jensen.johanna.auctionsite.dto.my.MyActiveBids;
import se.jensen.johanna.auctionsite.event.BidPlacedEvent;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.BidMapper;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.BidRepository;
import se.jensen.johanna.auctionsite.repository.MaxBidRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

  private final BidRepository bidRepository;
  private final AuctionRepository auctionRepository;
  private final UserRepository userRepository;
  private final BidMapper bidMapper;
  private final MaxBidRepository maxBidRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Retrieves a list of all bids for an auction
   *
   * @param auctionId ID of auction to fetch bids for
   * @return {@link BidHistoryResponse} a list of all bids with an Integer as an alias for the
   * bidder
   */
  @Transactional(readOnly = true)
  public List<BidHistoryResponse> getBidsForActiveAuction(Long auctionId) {
    Auction auction = auctionRepository.findWithBidsAndBiddersById(auctionId).orElseThrow(() ->
        new NotFoundException(String.format("Auction with id %d not found.", auctionId)));

    AtomicInteger counter = new AtomicInteger(1);
    Map<Long, Integer> userIdAlias = new HashMap<>();

    auction.getBids().stream()
        .sorted(Comparator.comparing(Bid::getCreatedAt))
        .forEach(bid -> userIdAlias.computeIfAbsent(
            bid.getBidder().getId(),
            key -> counter.getAndIncrement()
        ));

    return auction.getBids().stream().map(bid -> {
      Long bidderId = bid.getBidder().getId();
      Integer alias = userIdAlias.get(bidderId);
      return new BidHistoryResponse(bidderId, bid.getBidSum(), bid.getCreatedAt(), bid.getIsAuto(),
          alias);
    }).toList();
  }

  /**
   * Retrieves a list of all active bids for authenticated user
   *
   * @param userId ID of user to fetch bids for
   * @return a list of {@link MyActiveBids} contains information about the bids and auction
   */
  @Transactional(readOnly = true)
  public List<MyActiveBids> getMyActiveBids(Long userId) {
    List<Bid> userBids = bidRepository.findLatestActiveUserBids(userId, AuctionStatus.ACTIVE);
    List<Long> auctionIds = userBids.stream().map(Bid::getAuction).map(Auction::getId).toList();
    List<Object[]> maxSumResult = maxBidRepository.findMaxBidSumByAuctionAndUser_IdIn(userId,
        auctionIds);

    Map<Long, Integer> maxBidSums = maxSumResult.stream().collect(Collectors.toMap(
        row -> ((Number) row[0]).longValue(),
        row -> ((Number) row[1]).intValue()
    ));

    return userBids.stream().map(b -> {
      boolean isLeading = b.getAuction().getWinningBid()
          .map(leading -> leading.getBidder().getId().equals(userId)).orElse(false);
      boolean isAcceptedMet = b.getAuction().acceptedMet();
      BidStatus status =
          isLeading ? isAcceptedMet ? BidStatus.LEADING : BidStatus.BELOW_ACCEPTED_LEADING
              : BidStatus.OUTBID;
      Integer maxSum = maxBidSums.get(b.getAuction().getId());
      return bidMapper.toMyActiveBids(b, status, maxSum);
    }).toList();
  }

  /**
   * Places a bid on an auction with automatic retry on optimistic lock failures.
   *
   * @param bidRequest The bid details.
   * @param userId     The ID of the bidder.
   * @param auctionId  The ID of the auction.
   * @return A {@link BidResponse} the result of the bidding process.
   */
  @Retryable(retryFor = {OptimisticLockingFailureException.class},
      backoff = @Backoff(delay = 100)
  )
  @Transactional
  public BidResponse placeBid(BidRequest bidRequest, Long userId, Long auctionId) {
    Auction auction = auctionRepository.findByIdForBidding(auctionId).orElseThrow(() ->
        new NotFoundException(String.format("Auction with id %d not found", auctionId)));
    User bidder = userRepository.findById(userId).orElseThrow(() ->
        new NotFoundException(String.format("User with id %d not found", userId)));
    if (bidder.getId().equals(auction.getItem().getSeller().getId())) {
      throw new DomainArgumentException("You can not bid on your own item.");
    }
    int amount = bidRequest.amount();
    log.info("Attempting to place bid - user {}, auction {}, amount {}", userId, auctionId, amount);
    BiddingResult result = auction.placeBid(bidder, amount);

    // crucial to save winner last for id and created at sorting
    // if bidder was raising with a max bid, new bid is null
    if (result.newBidderLeads()) {
      if (result.otherBid() != null) {
        bidRepository.save(result.otherBid());
      }
      if (result.newBid() != null) {
        bidRepository.save(result.newBid());
      }
    } else {
      if (result.newBid() != null) {
        bidRepository.save(result.newBid());
      }
      if (result.otherBid() != null) {
        bidRepository.save(result.otherBid());
      }
    }

    auctionRepository.save(auction);
    log.info(
        "Bid placed - user {}, auction {}, leading: {}, is auto: {}",
        userId,
        auctionId,
        result.newBidderLeads(),
        result.isAuto()
    );
    eventPublisher.publishEvent(new BidPlacedEvent(auctionId));
    return createBidResponse(result, auction);
  }

  public BidResponse createBidResponse(BiddingResult result, Auction auction) {
    int currentHighest = auction.getWinningBid().map(Bid::getBidSum).orElse(0);
    int bidSum = result.newBid() != null ? result.newBid().getBidSum() : 0;
    Integer maxBidSum = result.maxBid() != null ? result.maxBid().getMaxSum() : null;
    BidStatus status = result.newBidderLeads() ? auction.acceptedMet() ? BidStatus.LEADING
        : BidStatus.BELOW_ACCEPTED_LEADING : BidStatus.OUTBID;
    return new BidResponse(bidSum, status, currentHighest, result.isAuto(), maxBidSum);
  }
}
