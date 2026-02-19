package se.jensen.johanna.auctionsite.dto.my;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactInfoRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9+\\s-]{8,15}$", message = "Invalid phonenumber format")
        String phoneNr
) {
}
