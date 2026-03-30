package se.jensen.johanna.auctionsite.controller.rest;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.auctionsite.dto.AuctionResponse;
import se.jensen.johanna.auctionsite.dto.AuctionsListResponse;
import se.jensen.johanna.auctionsite.dto.BidHistoryResponse;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BidResponse;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.AuctionService;
import se.jensen.johanna.auctionsite.service.BidService;
import se.jensen.johanna.auctionsite.util.JwtUtils;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin
@RequiredArgsConstructor
public class AuctionController {

  private final AuctionService auctionService;
  private final BidService bidService;
  private final JwtUtils jwtUtils;

  /**
   * Retrieves a paginated list of auctions with status ACTIVE for public users to scroll Optional
   * sorting of category and subcategory
   */
  @GetMapping
  public ResponseEntity<Page<AuctionsListResponse>> getAllActiveAuctions(
      @ParameterObject @PageableDefault(size = 20, sort = "endTime", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Category.SubCategory subCategory) {
    return ResponseEntity.ok(auctionService.getAllActiveAuctions(category, subCategory, pageable));
  }

  @GetMapping("/{auctionId}")
  public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long auctionId) {
    return ResponseEntity.ok(auctionService.getActiveAuction(auctionId));
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{auctionId}/bid")
  public ResponseEntity<BidResponse> placeBid(@AuthenticationPrincipal Jwt jwt,
      @PathVariable Long auctionId, @RequestBody @Valid BidRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bidService.placeBid(request, jwtUtils.extractUserId(jwt), auctionId));
  }

  @GetMapping("/{auctionId}/bids")
  public ResponseEntity<List<BidHistoryResponse>> getBidHistory(@PathVariable Long auctionId) {
    return ResponseEntity.ok(bidService.getBidsForActiveAuction(auctionId));
  }


  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{auctionId}/my-max-bid")
  public ResponseEntity<Integer> getMyMaxBidForAuction(@PathVariable Long auctionId,
      @AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(bidService.getMyMaxBid(jwtUtils.extractUserId(jwt), auctionId));
  }


  @GetMapping("/categories")
  public ResponseEntity<List<Category>> getCategories() {
    return ResponseEntity.ok(Arrays.asList(Category.values()));
  }

  @GetMapping("/subcategories")
  public ResponseEntity<Map<Category, List<Category.SubCategory>>> getSubCategories() {
    Map<Category, List<Category.SubCategory>> subCategories = Arrays.stream(Category.values())
        .collect(
            Collectors.toMap(category -> category, Category::getAllSubsByCategory));
    return ResponseEntity.ok(subCategories);
  }
}
