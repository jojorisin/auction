package se.jensen.johanna.auctionsite.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterUserRequest(
        @Email
        @NotBlank(message = "Please enter valid email.")
        String email,

        @NotBlank(message = "Password cant be empty.")
        @Size(min = 8, message = "Must be minimum 8 characters")
        String password,

        @NotBlank(message = "Please confirm password")
        @Size(min = 8, message = "Passwords has to match")
        String confirmPassword) {


}
