package se.jensen.johanna.auctionsite.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.auctionsite.dto.auth.LoginRequest;
import se.jensen.johanna.auctionsite.dto.auth.LoginResponse;
import se.jensen.johanna.auctionsite.dto.auth.LoginResult;
import se.jensen.johanna.auctionsite.dto.auth.RefreshResponse;
import se.jensen.johanna.auctionsite.dto.auth.RefreshResult;
import se.jensen.johanna.auctionsite.dto.auth.RegisterUserRequest;
import se.jensen.johanna.auctionsite.service.AuthService;
import se.jensen.johanna.auctionsite.service.UserService;
import se.jensen.johanna.auctionsite.util.CookieUtils;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final CookieUtils cookieUtils;
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<LoginResponse> registerUser(
      @RequestBody @Valid RegisterUserRequest registerRequest) {
    userService.registerUser(registerRequest);
    LoginResult result = authService.login(
        new LoginRequest(registerRequest.email(), registerRequest.password()));
    ResponseCookie responseCookie = cookieUtils.createRefreshTokenCookie(result.refreshToken());
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(result.loginResponse());
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
    LoginResult result = authService.login(loginRequest);
    ResponseCookie responseCookie = cookieUtils.createRefreshTokenCookie(result.refreshToken());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(result.loginResponse());
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponse> refresh(
      @CookieValue(name = "refreshToken", required = false) String oldRefreshStr) {
    if (oldRefreshStr == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    RefreshResult result = authService.refresh(oldRefreshStr);
    ResponseCookie responseCookie = cookieUtils.createRefreshTokenCookie(result.refreshToken());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(new RefreshResponse(result.accessToken()));
  }

  /**
   * Logs out a appUser by deleting their refresh token and clearing the cookie.
   *
   * @param refreshTokenStr the refresh token from cookie, optional
   * @return empty Response with clear header
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshTokenStr

  ) {
    if (refreshTokenStr != null) {
      authService.logout(refreshTokenStr);
    }
    ResponseCookie cleanCookie = cookieUtils.getCleanResponseCookie();

    return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
        .build();
  }
}
