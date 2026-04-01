package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;

@Entity
@Table(name = "refresh_tokens")
@AttributeOverride(name = "id", column = @Column(name = "refresh_token_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Getter
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private AppUser appUser;

  @Column(nullable = false)
  Instant expiryDate;

  public static RefreshToken create(AppUser appUser, long durationMs) {
    if (appUser == null) {
      throw new DomainArgumentException("AppUser is required to create refreshToken");
    }
    if (durationMs <= 0) {
      throw new DomainArgumentException("Duration must be greater than 0");
    }
    return RefreshToken.builder()
        .appUser(appUser)
        .expiryDate(Instant.now().plusMillis(durationMs))
        .token(UUID.randomUUID().toString())
        .build();
  }

  public boolean isExpired() {
    return Instant.now().isAfter(this.getExpiryDate());
  }
}
