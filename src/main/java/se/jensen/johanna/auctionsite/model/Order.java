package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.exception.DomainStateException;
import se.jensen.johanna.auctionsite.model.enums.OrderStatus;

@Entity
@Table(name = "orders")
@AttributeOverride(name = "id", column = @Column(name = "order_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "auction_id", nullable = false)
  private Auction auction;

  @ManyToOne
  @JoinColumn(name = "buyer_id")
  private User buyer;

  @ManyToOne
  @JoinColumn(name = "seller_id")
  private User seller;

  @Column(name = "order_sum", nullable = false)
  BigDecimal orderSum;

  @Column(name = "stripe_session_id")
  private String stripeSessionId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;

  /**
   * Creates an order
   *
   * @param auction
   * @param seller   seller for the item
   * @param orderSum sum to be paid
   * @param buyer    winning bidder
   */
  public static Order create(Auction auction, User seller, Integer orderSum, User buyer) {
    if (auction == null) {
      throw new DomainArgumentException("Auction is required");
    }
    if (buyer == null) {
      throw new DomainArgumentException("Buyer is required at creation of order");
    }
    if (seller == null) {
      throw new DomainArgumentException("Seller is required at creation of order");
    }
    if (orderSum == null) {
      throw new DomainArgumentException("OrderSum is required");
    }
    if (orderSum <= 0) {
      throw new DomainArgumentException("OrderSum must be greater than 0");
    }
    if (!auction.getItem().getSeller().equals(seller)) {
      throw new DomainStateException("Seller must match item's seller");
    }

    BigDecimal sum = BigDecimal.valueOf(orderSum);
    return Order.builder()
        .auction(auction)
        .seller(seller)
        .orderSum(sum)
        .buyer(buyer)
        .status(OrderStatus.PENDING)
        .build();
  }

  public void assignStripeSession(String stripeSessionId) {
    this.stripeSessionId = stripeSessionId;
  }

  public void markAsPaid() {
    if (!status.equals(OrderStatus.PENDING)) {
      throw new DomainStateException("Order needs to be pending to be marked as paid.");
    }
    this.status = OrderStatus.PAID;
  }
}
