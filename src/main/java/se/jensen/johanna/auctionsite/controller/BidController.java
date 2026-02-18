package se.jensen.johanna.auctionsite.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.BidHistoryDTO;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.dto.BidResponse;
import se.jensen.johanna.auctionsite.service.BidService;
import se.jensen.johanna.auctionsite.util.JwtUtils;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class BidController {
    private final BidService bidService;
    private final JwtUtils jwtUtils;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/auctions/{auctionId}/bid")
    public ResponseEntity<BidResponse> placeBid(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long auctionId,
            @RequestBody @Valid BidRequest bidRequest
    ) {
        BidResponse responseDTO = bidService.placeBid(bidRequest, jwtUtils.extractUserId(jwt), auctionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/auctions/{auctionId}/bid")
    public ResponseEntity<List<BidHistoryDTO>> getBidHistory(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Optional<Long> userId = Optional.ofNullable(jwt).map(jwtUtils::extractUserId);
        return ResponseEntity.ok(bidService.getBidsForActiveAuction(auctionId, userId));
    }
}
