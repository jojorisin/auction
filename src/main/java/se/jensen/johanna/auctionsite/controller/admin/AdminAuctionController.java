package se.jensen.johanna.auctionsite.controller.admin;

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
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.admin.*;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.AuctionService;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/auctions")
@RequiredArgsConstructor
public class AdminAuctionController {
    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<AdminAuctionResponse> addAuction(@RequestBody @Valid CreateAuctionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.createAuctionForItem(request));
    }

    @GetMapping
    public ResponseEntity<Page<AdminAuctionResponse>> getAllAuctions(
            @ParameterObject @PageableDefault(size = 20, sort = "endTime", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Category.SubCategory subCategory
    ) {
        return ResponseEntity.ok(auctionService.findAllAuctions(category, subCategory, pageable));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AdminAuctionResponse> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuction(auctionId));
    }

    @PutMapping("/launch-batch")
    public ResponseEntity<LaunchBatchResponse> launchAuctions(
            @RequestBody(required = false) @Valid LaunchBatchRequest request
    ) {
        return ResponseEntity.ok(auctionService.launchBatch(request));
    }

    @PutMapping("/{auctionId}/launch")
    public ResponseEntity<ManualLaunchResponse> launchAuction(
            @PathVariable Long auctionId,
            @RequestBody @Valid ManualLaunchRequest request
    ) {
        return ResponseEntity.ok(auctionService.manualLaunch(auctionId, request));
    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<AdminAuctionResponse> updateAuction(
            @PathVariable Long auctionId,
            @RequestBody @Valid UpdateAuctionRequest request
    ) {
        return ResponseEntity.ok(auctionService.updateAuction(auctionId, request));
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long auctionId) {
        auctionService.deleteAuction(auctionId);
        return ResponseEntity.noContent().build();
    }
}
