package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.BidHistoryDTO;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BidResponse;
import se.jensen.johanna.auctionsite.dto.BiddingResult;
import se.jensen.johanna.auctionsite.dto.enums.BidStatus;
import se.jensen.johanna.auctionsite.dto.my.MyActiveBids;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.mapper.BidMapper;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.BidRepository;
import se.jensen.johanna.auctionsite.repository.MaxBidRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BidService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final MaxBidRepository maxBidRepository;

    /**
     * Retrieves a list of all bids for an auction
     * Optional user id if user is logged in it displays if bid belongs to them
     *
     * @param auctionId ID of auction to fetch bids for
     * @return {@link BidHistoryDTO} a list of all bids with an Integer as an alias for the bidder
     */
    public List<BidHistoryDTO> getBidsForActiveAuction(Long auctionId, Long currentUserId) {
        Auction auction = auctionRepository.findWithBidsAndBiddersById(auctionId).orElseThrow(NotFoundException::new);

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
            int alias = userIdAlias.get(bidderId);
            boolean isMe = Optional.ofNullable(currentUserId).map(id -> id.equals(bidderId)).orElse(false);

            return new BidHistoryDTO(bid.getBidSum(), bid.getCreatedAt(), bid.getIsAuto(), alias, isMe);
        }).toList();
    }

    /**
     * Retrieves a list of all active bids for authenticated user
     *
     * @param userId ID of user to fetch bids for
     * @return a list of {@link MyActiveBids} contains information about the bids and auction
     */
    public List<MyActiveBids> getMyActiveBids(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d not found.", userId));
        }
        List<Bid> userBids = bidRepository.findLatestActiveUserBids(userId, AuctionStatus.ACTIVE);

        List<Long> auctionIds = userBids.stream().map(Bid::getAuction).map(Auction::getId).toList();
        List<Object[]> maxSumResult = maxBidRepository.findMaxBidSumByAuctionAndUser_IdIn(userId, auctionIds);
        Map<Long, Integer> maxBidSums = maxSumResult.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).intValue()
        ));

        return userBids.stream().map(b -> {
            boolean isLeading = b.getAuction().getWinningBid()
                                 .map(leading -> leading.getBidder().getId().equals(userId)).orElse(false);
            BidStatus status = isLeading ? BidStatus.LEADING : BidStatus.OUTBID;
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
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(NotFoundException::new);
        User bidder = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        int amount = bidRequest.amount();

        BiddingResult result = auction.placeBid(bidder, amount);

        // crucial to save winner last for id and created at sorting
        // if bidder was raising with a max bid, new bid is null
        if (result.newBidderLeads()) {
            if (result.otherBid() != null) bidRepository.save(result.otherBid());
            if (result.newBid() != null) bidRepository.save(result.newBid());
        } else {
            if (result.newBid() != null) bidRepository.save(result.newBid());
            if (result.otherBid() != null) bidRepository.save(result.otherBid());
        }

        auctionRepository.save(auction);
        return createBidResponse(result, auction);
    }

    public BidResponse createBidResponse(BiddingResult result, Auction auction) {
        BidStatus status = result.newBidderLeads() ? BidStatus.LEADING : BidStatus.OUTBID;
        int currentHighest = auction.getWinningBid().map(Bid::getBidSum).orElse(0);
        int bidSum = result.newBid() != null ? result.newBid().getBidSum() : 0;
        Integer maxBidSum = result.maxBid() != null ? result.maxBid().getMaxSum() : null;
        return new BidResponse(bidSum, status, currentHighest, result.isAuto(), maxBidSum);
    }
}
