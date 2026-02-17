package se.jensen.johanna.auctionsite.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b FROM Bid b WHERE b.auction.id=:auctionId AND b.auction.status='ACTIVE' " +
            "AND b.bidSum > 0  ORDER BY b.createdAt DESC, b.id DESC")
    List<Bid> findAllActiveBidsForAuction(Long auctionId);


    @Query("SELECT b.bidSum FROM Bid b WHERE b.auction.id=:auctionId " +
            "ORDER BY b.bidSum DESC LIMIT 1")
    Optional<Integer> findHighestBidSum(@Param("auctionId") Long auctionId);


    @EntityGraph(attributePaths = {"auction.winningBid.bidder"})
    @Query("SELECT b FROM Bid b WHERE b.bidder.id = :userId " +
            "AND (:status IS NULL OR b.auction.status = :status) " +
            "AND b.createdAt = (SELECT MAX(b2.createdAt) FROM Bid b2 " +
            "                   WHERE b2.bidder.id = :userId " +
            "                   AND b2.auction = b.auction)")
    List<Bid> findLatestActiveUserBids(
            @Param("userId") Long userId,
            @Param("status") AuctionStatus status
    );


}
