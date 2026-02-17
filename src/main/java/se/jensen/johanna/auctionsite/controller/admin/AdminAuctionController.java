package se.jensen.johanna.auctionsite.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.ManualLaunchRequest;
import se.jensen.johanna.auctionsite.dto.admin.*;
import se.jensen.johanna.auctionsite.service.AuctionService;

import java.time.Instant;
import java.util.List;

@PreAuthorize("hasRole('ADMIN') ")
@RestController
@RequestMapping("/admin/auctions")
@RequiredArgsConstructor
public class AdminAuctionController {
    private final AuctionService auctionService;


    @GetMapping
    public ResponseEntity<List<AdminAuctionResponse>> getAllAuctions() {
        List<AdminAuctionResponse> adminAuctionResponses =
                auctionService.findAllAuctions();
        return ResponseEntity.ok(adminAuctionResponses);
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AdminAuctionResponse> getAuction(@PathVariable Long auctionId) {
        AdminAuctionResponse auctionResponse = auctionService.getAuction(auctionId);
        return ResponseEntity.ok(auctionResponse);

    }

    //@PermitAll
    @PostMapping
    public ResponseEntity<AdminAuctionResponse> addAuction(@RequestBody CreateAuctionRequest request) {
        AdminAuctionResponse response = auctionService.createAuctionForItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/launch-batch")
    public ResponseEntity<LaunchBatchResponse> launchAuctions(
            @RequestParam(name = "size", defaultValue = "100") int size,
            @RequestParam(name = "startTime", required = false) Instant startTime,
            @RequestParam(name = "endTime", required = false) Instant endTime
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.launchBatch(size, startTime, endTime));


    }

    @PostMapping("{auctionId}")
    public ResponseEntity<ManualLaunchResponse> launchAuction(
            @PathVariable Long auctionId,
            @RequestBody @Valid ManualLaunchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.manualLaunch(auctionId, request));

    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<AdminAuctionResponse> updateAuction(
            @PathVariable Long auctionId,
            @RequestBody UpdateAuctionRequest request
    ) {
        AdminAuctionResponse response = auctionService.updateAuction(auctionId, request);
        return ResponseEntity.ok().body(response);

    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long auctionId) {
        auctionService.deleteAuction(auctionId);
        return ResponseEntity.noContent().build();

    }

}
