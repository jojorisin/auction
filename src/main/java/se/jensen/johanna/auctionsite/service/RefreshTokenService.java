package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.exception.RefreshTokenException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.model.AppUser;
import se.jensen.johanna.auctionsite.model.RefreshToken;
import se.jensen.johanna.auctionsite.repository.RefreshTokenRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshTokenDurationMs;

  private final RefreshTokenRepository refreshTokenRepository;

  private final UserRepository userRepository;

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken createRefreshToken(Long userId) {
    AppUser appUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    refreshTokenRepository.deleteByAppUser(appUser);
    refreshTokenRepository.flush();
    RefreshToken newRefreshToken = RefreshToken.create(appUser, refreshTokenDurationMs);
    refreshTokenRepository.save(newRefreshToken);
    log.info("Created new refresh token for appUser {}.", userId);
    return newRefreshToken;
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.isExpired()) {
      log.info("Refresh token has expired for appUser {}.", token.getAppUser().getId());
      refreshTokenRepository.delete(token);
      throw new RefreshTokenException("Refresh token has expired. Please Log in again.");
    }
    return token;
  }

  public void deleteRefreshToken(String refreshToken) {
    findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);

  }
}
