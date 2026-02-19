package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

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
    private User user;

    @Column(nullable = false)
    Instant expiryDate;

    public static RefreshToken create(User user, long durationMs) {
        if (user == null) throw new IllegalArgumentException("User is required to create refreshToken");
        if (durationMs <= 0) throw new IllegalArgumentException("Duration must be greater than 0");
        return RefreshToken.builder()
                           .user(user)
                           .expiryDate(Instant.now().plusMillis(durationMs))
                           .token(UUID.randomUUID().toString())
                           .build();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.getExpiryDate());
    }
}
