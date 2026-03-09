package se.jensen.johanna.auctionsite.controller;

import com.stripe.exception.StripeException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.auctionsite.dto.CheckoutResponse;
import se.jensen.johanna.auctionsite.service.PaymentService;
import se.jensen.johanna.auctionsite.util.JwtUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentService paymentService;
  private final JwtUtils jwtUtils;

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/checkout-session/{orderId}")
  public ResponseEntity<CheckoutResponse> createCheckoutSession(@PathVariable Long orderId,
      @AuthenticationPrincipal Jwt jwt) throws StripeException {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(paymentService.createCheckoutSession(orderId, jwtUtils.extractUserId(jwt)));
  }

  @PostMapping("/webhook")
  public ResponseEntity<Void> handleWebhook(@RequestBody byte[] payload,
      @RequestHeader("stripe-signature") String signature) throws StripeException {
    String payloadString = new String(payload, StandardCharsets.UTF_8);
    paymentService.handleWebhook(new String(payload, StandardCharsets.UTF_8), signature);
    log.info("Stripe payload: {}", payloadString);
    return ResponseEntity.ok().build();
  }
}
