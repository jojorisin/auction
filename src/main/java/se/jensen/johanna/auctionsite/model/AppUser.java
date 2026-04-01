package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.exception.InvalidPhoneNumberException;
import se.jensen.johanna.auctionsite.model.enums.Role;

@Entity
@Table(name = "users")
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AppUser extends BaseEntity {

  @Column(nullable = false, unique = true, updatable = false)
  @NotNull
  private String email;

  @Column(nullable = false)
  @NotNull
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(nullable = false)
  @NotNull
  private String hashedPassword;

  //create value object money?
  private int payments;

  private String phoneNr;

  @Embedded
  @Valid
  private Address address;

  public static AppUser register(String email, String hashedPassword, Role role) {
    if (email == null || email.isBlank()) {
      throw new DomainArgumentException("Email required.");
    }
    if (hashedPassword == null || hashedPassword.isBlank()) {
      throw new DomainArgumentException("Password required.");
    }
    if (role == null) {
      throw new DomainArgumentException("Role required.");
    }

    return AppUser.builder().email(email).hashedPassword(hashedPassword).role(role).build();
  }

  public void changeAddress(Address address) {
    if (address == null) {
      throw new DomainArgumentException("Address is required");
    }
    this.address = address;
  }

  public void changePassword(String hashedPassword) {
    if (hashedPassword == null || hashedPassword.isBlank()) {
      throw new DomainArgumentException("Password is required");
    }
    this.hashedPassword = hashedPassword;
  }

  public void changeContactInfo(String rawPhoneNr) {
    if (rawPhoneNr == null || rawPhoneNr.isBlank()) {
      throw new DomainArgumentException("Phone number is required");
    }
    String cleanPhoneNr = rawPhoneNr.trim().replaceAll("[^0-9+]", "");
    if (cleanPhoneNr.isEmpty()) {
      throw new InvalidPhoneNumberException("Please enter a valid phone number.");
    }
    this.phoneNr = cleanPhoneNr;
  }
}
