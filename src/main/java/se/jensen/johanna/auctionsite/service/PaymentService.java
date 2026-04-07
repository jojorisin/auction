package se.jensen.johanna.auctionsite.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.CheckoutResponse;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.model.Order;
import se.jensen.johanna.auctionsite.model.enums.OrderStatus;
import se.jensen.johanna.auctionsite.repository.OrderRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  @Value("${stripe.api-key}")
  private String stripeApiKey;

  @Value("${stripe.success-url}")
  private String stripeSuccessUrl;

  @Value("${stripe.cancel-url}")
  private String stripeCancelUrl;

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  private final OrderRepository orderRepository;

  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeApiKey;
    log.info("Loaded webhook secret: {}", webhookSecret);
  }

  @Transactional
  public CheckoutResponse createCheckoutSession(Long orderId, Long userId) throws StripeException {
    Order order = orderRepository.findByIdAndBuyer_Id(orderId, userId)
        .orElseThrow(() -> new NotFoundException("Order not found."));
    long amountInCents = order.getOrderSum().multiply(BigDecimal.valueOf(100)).longValue();

    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT).setCustomerEmail(order.getBuyer().getEmail())
        .setSuccessUrl(stripeSuccessUrl + "{CHECKOUT_SESSION_ID}").setCancelUrl(stripeCancelUrl)
        .addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(
            SessionCreateParams.LineItem.PriceData.builder().setCurrency("sek")
                .setUnitAmount(amountInCents).setProductData(
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Auction: " + order.getAuction().getItem().getTitle()).build())
                .build()).build()).putMetadata("orderId", orderId.toString()).build();
    Session session = Session.create(params);
    order.assignStripeSession(session.getId());
    orderRepository.save(order);
    return new CheckoutResponse(session.getUrl(), session.getId());
  }

  @Transactional
  public void handleWebhook(String payload, String signature) throws StripeException {
    Event event = Webhook.constructEvent(payload, signature, webhookSecret);
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

    switch (event.getType()) {
      case "checkout.session.completed":
        Session session;
        if (dataObjectDeserializer.getObject().isPresent()) {
          session = (Session) dataObjectDeserializer.getObject().get();
        } else {
          session = (Session) dataObjectDeserializer.deserializeUnsafe();
          log.warn("Using unsafe deserializer for session: {}", session);
        }
        String orderId = session.getMetadata().get("orderId");
        if (session.getPaymentStatus().equals("paid") && orderId != null) {
          processOrderPayment(session);
        }
        break;
      default:
        log.info("Ignoring event type: {}", event.getType());
        break;
    }
  }

  private void processOrderPayment(Session session) {
    Long orderId = Long.parseLong(session.getMetadata().get("orderId"));
    Order order = getOrder(orderId);

    if (order.getStatus() == OrderStatus.PAID) {
      log.info("Order {} already handled.", orderId);
      return;
    }

    order.markAsPaid();
    orderRepository.save(order);
    log.info("Order {} marked as paid.", orderId);
  }

  private Order getOrder(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(
        () -> new NotFoundException(String.format("Order with id %s not found", orderId)));
  }

}
