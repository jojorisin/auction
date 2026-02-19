package se.jensen.johanna.auctionsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.MaxBid;

import java.util.List;

@Repository
public interface MaxBidRepository extends JpaRepository<MaxBid, Long> {

    @Query("SELECT m.auction.id, MAX (m.maxSum) FROM MaxBid m WHERE m.bidder.id=:userId AND m.auction.id IN :auctionIds GROUP BY m.auction.id")
    List<Object[]> findMaxBidSumByAuctionAndUser_IdIn(
            @Param("userId") Long userId,
            @Param("auctionIds") List<Long> auctionIds
    );
}

