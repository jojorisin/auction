package se.jensen.johanna.auctionsite.dto.my;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordDTO(
        @NotBlank(message = "Write your new password")
        @Size(min = 8, message = "Must be atleast 8 characters")
        String newPassword,
        @NotBlank(message = "Confirm your new password. Type is case-sensitive.")
        String confirmNewPassword,
        @NotBlank(message = "Write your old password. Type is case-sensitive.")
        String oldPassword) {


}
