package se.jensen.johanna.auctionsite.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.LoginResponse;
import se.jensen.johanna.auctionsite.dto.LoginResult;
import se.jensen.johanna.auctionsite.dto.RefreshResponse;
import se.jensen.johanna.auctionsite.dto.RefreshResult;
import se.jensen.johanna.auctionsite.dto.auth.LoginRequest;
import se.jensen.johanna.auctionsite.dto.auth.RegisterUserRequest;
import se.jensen.johanna.auctionsite.service.AuthService;
import se.jensen.johanna.auctionsite.service.UserService;
import se.jensen.johanna.auctionsite.util.CookieUtils;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final CookieUtils cookieUtils;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerUser(
            @RequestBody @Valid RegisterUserRequest registerRequest
    ) {

        userService.registerUser(registerRequest);

        LoginResult result = authService.login(new LoginRequest(registerRequest.email(), registerRequest.password()));


        ResponseCookie responseCookie = cookieUtils.createRefreshTokenCookie(result.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(result.loginResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest loginRequest
    ) {

        LoginResult result = authService.login(loginRequest);


        ResponseCookie responseCookie = cookieUtils.createRefreshTokenCookie(result.refreshToken());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(result.loginResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @CookieValue(name = "refreshToken") String oldRefreshStr
    ) {

        RefreshResult result = authService.refresh(oldRefreshStr);
        ResponseCookie responseCookie = cookieUtils.createRefreshTokenCookie(result.refreshToken());


        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).
                             body(new RefreshResponse(result.accessToken()));


    }

    //logout


}
