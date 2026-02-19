package se.jensen.johanna.auctionsite.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.Bid;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @EntityGraph(attributePaths = {"auction.winningBid.bidder", "auction.item"})
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
