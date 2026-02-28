package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.ResponseMessage;
import se.jensen.johanna.auctionsite.dto.auth.RegisterUserRequest;
import se.jensen.johanna.auctionsite.dto.my.*;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.exception.PasswordMisMatchException;
import se.jensen.johanna.auctionsite.exception.UserAlreadyExistsException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.mapper.UserMapper;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.Role;
import se.jensen.johanna.auctionsite.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public void registerUser(RegisterUserRequest userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new UserAlreadyExistsException(String.format(
                    "User with email %s already exists. Try to log in.",
                    userDto.email()
            ));
        }
        if (!userDto.confirmPassword().equals(userDto.password())) {
            throw new PasswordMisMatchException("Passwords do not match. Please try again.");
        }
        String hashedPassword = passwordEncoder.encode(userDto.password());
        userRepository.save(User.register(userDto.email(), hashedPassword, Role.MEMBER));
        log.info("User with email {} registered successfully.", userDto.email());
    }

    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedUser(Long userId) {
        User user = getUserOrThrow(userId);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public ResponseMessage updatePassword(Long userId, UpdatePasswordRequest passwordDTO) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(passwordDTO.oldPassword(), user.getHashedPassword()) || !passwordDTO.newPassword()
                                                                                                         .equals(passwordDTO.confirmNewPassword())) {
            throw new PasswordMisMatchException("Passwords do not match. Please try again.");
        }
        String newHashedPw = passwordEncoder.encode(passwordDTO.newPassword());
        user.changePassword(newHashedPw);
        log.info("Password for user {} updated successfully.", userId);
        return new ResponseMessage("Password has been updated successfully.");
    }

    @Transactional
    public UserResponse updateContactInfo(Long userId, ContactInfoRequest request) {
        User user = getUserOrThrow(userId);
        user.changeContactInfo(request.phoneNr());
        log.info("Contact info for user {} updated successfully.", userId);
        return userMapper.toUserResponse(user);
    }

    /**
     * Creates and adds a new address to the specific user.
     *
     * @param userId  ID of the user that is updating address
     * @param request {@link AddressRequest} Contains address fields with validation
     * @return {@link AddressResponse} The newly updated address
     */
    @Transactional
    public AddressResponse updateAddress(Long userId, AddressRequest request) {
        User user = getUserOrThrow(userId);
        user.changeAddress(userMapper.toAddress(request));
        userRepository.save(user);
        log.info("Address for user {} updated successfully.", userId);
        return userMapper.toAddressResponse(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User with id %d not found.", userId)));
    }
}
