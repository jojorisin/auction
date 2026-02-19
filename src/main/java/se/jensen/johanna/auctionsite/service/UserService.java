package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.ResponseMessage;
import se.jensen.johanna.auctionsite.dto.auth.RegisterUserRequest;
import se.jensen.johanna.auctionsite.dto.my.*;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.exception.PasswordMisMatchException;
import se.jensen.johanna.auctionsite.exception.UserAlreadyExistsException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.mapper.UserMapper;
import se.jensen.johanna.auctionsite.model.Address;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.Role;
import se.jensen.johanna.auctionsite.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public void registerUser(RegisterUserRequest userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new UserAlreadyExistsException("Account is already registered with this email. Please try to login.");
        }
        if (!userDto.confirmPassword().equals(userDto.password())) {
            throw new PasswordMisMatchException("Passwords do not match. Please try again.");
        }
        String hashedPassword = passwordEncoder.encode(userDto.password());
        userRepository.save(User.register(userDto.email(), hashedPassword, Role.MEMBER));
    }

    public AppUserDTO getAuthenticatedUser(Long userId) {
        User user = getUserOrThrow(userId);
        return userMapper.toAppUserDTO(user);
    }

    public ResponseMessage updatePassword(UpdatePasswordDTO passwordDTO, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(passwordDTO.oldPassword(), user.getHashedPassword()) || !passwordDTO.newPassword()
                                                                                                         .equals(passwordDTO.confirmNewPassword())) {
            throw new PasswordMisMatchException("Passwords do not match. Please try again.");
        }
        String newHashedPw = passwordEncoder.encode(passwordDTO.newPassword());
        user.changePassword(newHashedPw);
        return new ResponseMessage("Password has been updated successfully.");
    }

    public AppUserDTO updateContactInfo(Long userId, ContactInfoRequest request) {
        User user = getUserOrThrow(userId);
        user.changeContactInfo(request.phoneNr());
        return userMapper.toAppUserDTO(user);
    }

    /**
     * Creates and adds a new address to the specific user.
     *
     * @param userId  ID of the user that is updating address
     * @param request {@link AddressRequest} Contains address fields with validation
     * @return {@link AddressResponse} The newly updated address
     */
    public AddressResponse updateAddress(Long userId, AddressRequest request) {
        User user = getUserOrThrow(userId);
        Address address = Address.create(
                request.firstName(),
                request.lastName(),
                request.co(),
                request.streetName(),
                request.streetName2(),
                request.postalCode(),
                request.city(),
                request.country()
        );
        user.changeAddress(address);
        userRepository.save(user);
        return userMapper.toAddressResponse(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User with id %d not found.", userId))
        );
    }
}
