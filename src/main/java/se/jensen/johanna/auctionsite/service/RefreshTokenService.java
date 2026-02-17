package se.jensen.johanna.auctionsite.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.exception.RefreshTokenException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.model.RefreshToken;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.repository.RefreshTokenRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

import java.util.Optional;

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
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();


        return refreshTokenRepository.save(RefreshToken.create(user, refreshTokenDurationMs));


    }

    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException("Refresh token has expired. Please Log in again.");
        }
        return token;

    }

}
