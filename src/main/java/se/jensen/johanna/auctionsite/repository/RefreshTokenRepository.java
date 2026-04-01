package se.jensen.johanna.auctionsite.repository;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.AppUser;
import se.jensen.johanna.auctionsite.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  @Transactional
  @Modifying
  void deleteByAppUser(AppUser appUser);
}
