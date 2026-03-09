package se.jensen.johanna.auctionsite.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  @EntityGraph(attributePaths = {"auction", "auction.item"})
  Optional<Order> findByIdAndBuyer_Id(Long orderId, Long userId);

  @EntityGraph(attributePaths = {"auction", "auction.item"})
  List<Order> findByBuyer_id(Long userId);

}
