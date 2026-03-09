package se.jensen.johanna.auctionsite.repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

  @Lock(LockModeType.OPTIMISTIC)
  @Query("SELECT a FROM Auction a WHERE a.id = :auctionId")
  Optional<Auction> findByIdForBidding(@Param("auctionId") Long auctionId);

  @EntityGraph(attributePaths = {"bids.bidder", "item"})
  Optional<Auction> findById(@NonNull Long auctionId);

  @EntityGraph(attributePaths = {"winningBid.bidder", "item.seller", "bids.bidder"})
  @Query("SELECT a FROM Auction a WHERE a.endTime<:now " +
      "AND (a.status='ACTIVE')")
  List<Auction> findEndedAuctionsWithBidsAndItemSeller(@Param("now") Instant now);

  @Query("SELECT a FROM Auction a WHERE a.status='ACTIVE'" +
      " AND ( :category IS NULL OR a.item.category=:category ) " +
      "AND ( :subCategory IS NULL OR a.item.subCategory=:subCategory )")
  Page<Auction> findActiveAuctions(
      @Param("category") Category category,
      @Param("subCategory") Category.SubCategory subCategory,
      Pageable pageable
  );

  @Query("SELECT a FROM Auction a WHERE" +
      " (:category IS NULL OR a.item.category=:category ) " +
      "AND ( :subCategory IS NULL OR a.item.subCategory=:subCategory )")
  Page<Auction> findAllAuctions(
      @Param("category") Category category,
      @Param("subCategory") Category.SubCategory subCategory,
      Pageable pageable
  );

  @EntityGraph(attributePaths = {"winningBid.bidder", "item"})
  @Query("SELECT a FROM Auction a WHERE a.winningBid.bidder.id=:userId AND a.status='SOLD'")
  List<Auction> findWonAuctionsByUserId(@Param("userId") Long userId);

  @EntityGraph(attributePaths = {"item"})
  Page<Auction> findByStatusOrderByCreatedAtAsc(AuctionStatus status, Pageable pageable);

  @Query("SELECT COUNT (a)>0 FROM Auction a WHERE a.item.id=:itemId AND a.status=:status")
  boolean existsByItemIdAndStatus(Long itemId, AuctionStatus status);

  @EntityGraph(attributePaths = {"bids.bidder"})
  Optional<Auction> findWithBidsAndBiddersById(Long auctionId);
}
