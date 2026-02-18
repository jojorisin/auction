package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.LoginResponse;
import se.jensen.johanna.auctionsite.dto.LoginResult;
import se.jensen.johanna.auctionsite.dto.RefreshResult;
import se.jensen.johanna.auctionsite.dto.auth.LoginRequest;
import se.jensen.johanna.auctionsite.exception.RefreshTokenException;
import se.jensen.johanna.auctionsite.model.RefreshToken;
import se.jensen.johanna.auctionsite.security.MyUserDetails;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final MyUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public LoginResult login(LoginRequest loginRequest) {
        Authentication auth = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        Authentication authenticatedAuth = authenticationManager.authenticate(auth);
        MyUserDetails userDetails = (MyUserDetails) authenticatedAuth.getPrincipal();
        String accessToken = tokenService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUserId());
        return new LoginResult(
                new LoginResponse(
                        accessToken,
                        userDetails.getUserId(),
                        userDetails.getRole(),
                        userDetails.getUsername()
                ),
                refreshToken.getToken()
        );
    }

    public RefreshResult refresh(String oldTokenStr) {
        RefreshToken oldToken = refreshTokenService.findByToken(oldTokenStr)
                                                   .map(refreshTokenService::verifyExpiration)
                                                   .orElseThrow(() -> new RefreshTokenException(
                                                           "RefreshToken is not in database"));

        MyUserDetails userDetails = (MyUserDetails) userDetailsService.loadUserByUsername(oldToken.getUser()
                                                                                                  .getEmail());
        String newAccessToken = tokenService.generateToken(userDetails);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userDetails.getUserId());
        return new RefreshResult(newAccessToken, newRefreshToken.getToken());
    }
}
