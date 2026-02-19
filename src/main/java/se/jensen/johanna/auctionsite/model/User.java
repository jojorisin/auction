package se.jensen.johanna.auctionsite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.jensen.johanna.auctionsite.exception.InvalidPhoneNumberException;
import se.jensen.johanna.auctionsite.model.enums.Role;

@Entity
@Table(name = "users")
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

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
    private Address address;

    public static User register(String email, String hashedPassword, Role role) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required.");
        if (hashedPassword == null || hashedPassword.isBlank())
            throw new IllegalArgumentException("Password required.");
        if (role == null) throw new IllegalArgumentException("Role required.");

        return User.builder().email(email).hashedPassword(hashedPassword).role(role).build();
    }

    public void changeAddress(Address address) {
        if (address == null) throw new IllegalArgumentException("Address is required");
        this.address = address;
    }

    public void changePassword(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isBlank())
            throw new IllegalArgumentException("Password is required");
        this.hashedPassword = hashedPassword;
    }

    public void changeContactInfo(String rawPhoneNr) {
        if (rawPhoneNr == null || rawPhoneNr.isBlank()) throw new IllegalArgumentException("Phone number is required");
        String cleanPhoneNr = rawPhoneNr.trim().replaceAll("[^0-9+]", "");
        if (cleanPhoneNr.isEmpty()) {
            throw new InvalidPhoneNumberException("Please enter a valid phone number.");
        }
        this.phoneNr = cleanPhoneNr;
    }
}
