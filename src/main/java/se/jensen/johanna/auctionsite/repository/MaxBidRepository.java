package se.jensen.johanna.auctionsite.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.MaxBid;

@Repository
public interface MaxBidRepository extends JpaRepository<MaxBid, Long> {

  @Query("SELECT m.auction.id, MAX (m.maxSum) FROM MaxBid m WHERE m.bidder.id=:userId AND m.auction.id IN :auctionIds GROUP BY m.auction.id")
  List<Object[]> findMaxBidSumByAuctionAndUser_IdIn(
      @Param("userId") Long userId,
      @Param("auctionIds") List<Long> auctionIds
  );

  @Query("SELECT MAX(m.maxSum) FROM MaxBid m WHERE m.auction.id = :auctionId AND m.bidder.id = :userId")
  Optional<Integer> findHighestMaxBidByUserAndAuctionId(Long userId, Long auctionId);
}

