package se.jensen.johanna.auctionsite.controller.my;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.ResponseMessage;
import se.jensen.johanna.auctionsite.dto.my.*;
import se.jensen.johanna.auctionsite.service.AuctionService;
import se.jensen.johanna.auctionsite.service.BidService;
import se.jensen.johanna.auctionsite.service.UserService;
import se.jensen.johanna.auctionsite.util.JwtUtils;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/my")
@CrossOrigin
@RequiredArgsConstructor
public class MyController {
    private final UserService userService;
    private final AuctionService auctionService;
    private final BidService bidService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<AppUserDTO> getMe(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok().body(
                userService.getAuthenticatedUser(jwtUtils.extractUserId(jwt))
        );

    }

    @PutMapping("/address")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid AddressRequest request
    ) {


        return ResponseEntity.ok().body(userService.updateAddress(
                jwtUtils.extractUserId(jwt), request
        ));

    }

    @PutMapping("/contact")
    public ResponseEntity<AppUserDTO> updateContactInfo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ContactInfoRequest request
    ) {
        return ResponseEntity.ok(userService.updateContactInfo(jwtUtils.extractUserId(jwt), request));

    }

    @PutMapping("/password")
    public ResponseEntity<ResponseMessage> updatePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UpdatePasswordDTO passwordDTO
    ) {
        ResponseMessage message = userService.updatePassword(passwordDTO, jwtUtils.extractUserId(jwt));
        return ResponseEntity.ok(message);
    }

    @GetMapping("/bidding")
    public ResponseEntity<List<MyActiveBids>> getActiveBids(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<MyActiveBids> myActiveBids = bidService.getMyActiveBids(jwtUtils.extractUserId(jwt));

        return ResponseEntity.ok(myActiveBids);
    }

    @GetMapping("/won")
    public ResponseEntity<List<MyWonAuctionDTO>> getMyWonAuctions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<MyWonAuctionDTO> wonAuctions = auctionService.getMyWonAuctions(jwtUtils.extractUserId(jwt));

        return ResponseEntity.ok(wonAuctions);

    }


}
