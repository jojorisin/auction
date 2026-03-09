package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.OrderRequest;
import se.jensen.johanna.auctionsite.dto.OrderResponse;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.OrderMapper;
import se.jensen.johanna.auctionsite.model.Order;
import se.jensen.johanna.auctionsite.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  @Transactional
  public void createOrder(OrderRequest request) {
    Order newOrder = Order.create(
        request.auction(),
        request.seller(),
        request.orderSum(),
        request.buyer()
    );
    orderRepository.save(newOrder);
  }

  public List<OrderResponse> getAllOrdersForUser(Long userId) {
    return orderRepository.findByBuyer_id(userId).stream().map(orderMapper::toOrderResponse)
        .toList();
  }

  public OrderResponse getOrderForUser(Long userId, Long orderId) {
    return orderRepository.findByIdAndBuyer_Id(orderId, userId).map(orderMapper::toOrderResponse)
        .orElseThrow(() -> new NotFoundException("Order not found."));
  }
}
