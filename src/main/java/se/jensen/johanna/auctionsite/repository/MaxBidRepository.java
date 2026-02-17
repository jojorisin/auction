package se.jensen.johanna.auctionsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.MaxBid;

@Repository
public interface MaxBidRepository extends JpaRepository<MaxBid, Long> {
}
