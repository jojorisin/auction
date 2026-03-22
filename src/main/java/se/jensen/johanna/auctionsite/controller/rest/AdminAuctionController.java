package se.jensen.johanna.auctionsite.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateAuctionRequest;
import se.jensen.johanna.auctionsite.dto.admin.LaunchRequest;
import se.jensen.johanna.auctionsite.dto.admin.LaunchResponse;
import se.jensen.johanna.auctionsite.dto.admin.UpdateAuctionRequest;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.AuctionScheduleService;
import se.jensen.johanna.auctionsite.service.AuctionService;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuctionController {

  private final AuctionService auctionService;
  private final AuctionScheduleService scheduleService;

  @PostMapping("/items/{itemId}/auctions")
  public ResponseEntity<AdminAuctionResponse> createAuction(
      @RequestBody @Valid CreateAuctionRequest request, @PathVariable Long itemId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(auctionService.createAuctionForItem(itemId, request));
  }

  @GetMapping("/auctions")
  public ResponseEntity<Page<AdminAuctionResponse>> getAllAuctions(
      @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Category.SubCategory subCategory) {
    return ResponseEntity.ok(auctionService.findAllAuctions(category, subCategory, pageable));
  }

  @GetMapping("/auctions/{auctionId}")
  public ResponseEntity<AdminAuctionResponse> getAuction(@PathVariable Long auctionId) {
    return ResponseEntity.ok(auctionService.getAuction(auctionId));
  }

  @PutMapping("/auctions/launch")
  public ResponseEntity<LaunchResponse> launchAuctions(
      @RequestBody @Valid LaunchRequest request) {
    return ResponseEntity.ok(auctionService.launchBatch(request));
  }

  @PutMapping("/auctions/{auctionId}")
  public ResponseEntity<AdminAuctionResponse> updateAuction(@PathVariable Long auctionId,
      @RequestBody @Valid UpdateAuctionRequest request) {
    return ResponseEntity.ok(auctionService.updateAuction(auctionId, request));
  }

  @DeleteMapping("/auctions/{auctionId}")
  public ResponseEntity<Void> deleteAuction(@PathVariable Long auctionId) {
    auctionService.deleteAuction(auctionId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/auctions/trigger-scheduler")
  public ResponseEntity<Void> triggerScheduler() {
    scheduleService.checkEndedAuctions();
    return ResponseEntity.ok().build();

  }
}
