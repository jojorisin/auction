package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.OrderRequest;
import se.jensen.johanna.auctionsite.model.Order;
import se.jensen.johanna.auctionsite.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

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
}
