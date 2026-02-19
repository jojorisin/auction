package se.jensen.johanna.auctionsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query("SELECT a FROM Auction a WHERE a.endTime<:now " +
            "AND (a.status='ACTIVE')")
    List<Auction> findEndedAuctionsWithBid(@Param("now") Instant now);

    @Query("SELECT a FROM Auction a WHERE a.status='ACTIVE'" +
            " AND ( :category IS NULL OR a.item.category=:category ) " +
            "AND ( :subCategory IS NULL OR a.item.subCategory=:subCategory )")
    Page<Auction> findActiveAuctions(
            @Param("category") Category category,
            @Param("subCategory") Category.SubCategory subCategory,
            Pageable pageable
    );

    @Query("SELECT a FROM Auction a WHERE a.winningBid.bidder.id=:userId AND a.status='SOLD'")
    List<Auction> findWonAuctionsByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"item"})
    Page<Auction> findByStatusOrderByCreatedAtAsc(AuctionStatus status, Pageable pageable);

    @Query("SELECT COUNT (a)>0 FROM Auction a WHERE a.item.id=:itemId AND (a.status='ACTIVE' OR a.status='PLANNED')")
    boolean existsByItemIdAndStatusActiveOrPlanned(Long itemId);

    @Query("SELECT COUNT(a)>0 FROM Auction a WHERE a.item.id=:itemId")
    boolean existsByItemId(Long itemId);

    @EntityGraph(attributePaths = {"bids.bidder"})
    Optional<Auction> findWithBidsAndBiddersById(Long auctionId);
}
