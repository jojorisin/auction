package se.jensen.johanna.auctionsite.controller.rest;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.auctionsite.dto.OrderResponse;
import se.jensen.johanna.auctionsite.dto.ResponseMessage;
import se.jensen.johanna.auctionsite.dto.my.AddressRequest;
import se.jensen.johanna.auctionsite.dto.my.AddressResponse;
import se.jensen.johanna.auctionsite.dto.my.ContactInfoRequest;
import se.jensen.johanna.auctionsite.dto.my.MyActiveBids;
import se.jensen.johanna.auctionsite.dto.my.MyItemResponse;
import se.jensen.johanna.auctionsite.dto.my.UpdatePasswordRequest;
import se.jensen.johanna.auctionsite.dto.my.UserResponse;
import se.jensen.johanna.auctionsite.dto.my.WonAuctionResponse;
import se.jensen.johanna.auctionsite.service.AuctionService;
import se.jensen.johanna.auctionsite.service.BidService;
import se.jensen.johanna.auctionsite.service.ItemService;
import se.jensen.johanna.auctionsite.service.OrderService;
import se.jensen.johanna.auctionsite.service.UserService;
import se.jensen.johanna.auctionsite.util.JwtUtils;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/me")
@CrossOrigin
@RequiredArgsConstructor
public class MyController {

  private final UserService userService;
  private final AuctionService auctionService;
  private final BidService bidService;
  private final JwtUtils jwtUtils;
  private final OrderService orderService;
  private final ItemService itemService;

  @GetMapping
  public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(userService.getAuthenticatedUser(jwtUtils.extractUserId(jwt)));
  }

  @GetMapping("/bids")
  public ResponseEntity<List<MyActiveBids>> getMyActiveBids(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(bidService.getMyActiveBids(jwtUtils.extractUserId(jwt)));
  }

  @GetMapping("/won")
  public ResponseEntity<List<WonAuctionResponse>> getMyWonAuctions(
      @AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(auctionService.getMyWonAuctions(jwtUtils.extractUserId(jwt)));
  }

  @GetMapping("/orders")
  public ResponseEntity<List<OrderResponse>> getAllMyOrders(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(orderService.getAllOrdersForUser(jwtUtils.extractUserId(jwt)));

  }

  @GetMapping("/orders/{orderId}")
  public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal Jwt jwt,
      @PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrderForUser(jwtUtils.extractUserId(jwt), orderId));
  }

  @GetMapping("/items")
  public ResponseEntity<List<MyItemResponse>> getAllMyItems(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(itemService.getAllItemsForSeller(jwtUtils.extractUserId(jwt)));
  }

  @GetMapping("/items/{itemId}")
  public ResponseEntity<MyItemResponse> getItem(@AuthenticationPrincipal Jwt jwt,
      @PathVariable Long itemId) {
    return ResponseEntity.ok(itemService.getItemForSeller(jwtUtils.extractUserId(jwt), itemId));
  }

  @PutMapping("/address")
  public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal Jwt jwt,
      @RequestBody @Valid AddressRequest request) {
    return ResponseEntity.ok(userService.updateAddress(jwtUtils.extractUserId(jwt), request));
  }

  @PutMapping("/contact")
  public ResponseEntity<UserResponse> updateContactInfo(@AuthenticationPrincipal Jwt jwt,
      @RequestBody @Valid ContactInfoRequest request) {
    return ResponseEntity.ok(userService.updateContactInfo(jwtUtils.extractUserId(jwt), request));
  }

  @PutMapping("/password")
  public ResponseEntity<ResponseMessage> updatePassword(@AuthenticationPrincipal Jwt jwt,
      @RequestBody @Valid UpdatePasswordRequest request) {
    return ResponseEntity.ok(userService.updatePassword(jwtUtils.extractUserId(jwt), request));
  }
}
