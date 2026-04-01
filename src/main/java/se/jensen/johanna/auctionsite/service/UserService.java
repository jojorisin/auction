package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.ResponseMessage;
import se.jensen.johanna.auctionsite.dto.auth.RegisterUserRequest;
import se.jensen.johanna.auctionsite.dto.my.AddressRequest;
import se.jensen.johanna.auctionsite.dto.my.AddressResponse;
import se.jensen.johanna.auctionsite.dto.my.ContactInfoRequest;
import se.jensen.johanna.auctionsite.dto.my.UpdatePasswordRequest;
import se.jensen.johanna.auctionsite.dto.my.UserResponse;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.exception.PasswordMisMatchException;
import se.jensen.johanna.auctionsite.exception.UserAlreadyExistsException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.mapper.UserMapper;
import se.jensen.johanna.auctionsite.model.AppUser;
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
          "AppUser with email %s already exists. Try to log in.",
          userDto.email()
      ));
    }
    if (!userDto.confirmPassword().equals(userDto.password())) {
      throw new PasswordMisMatchException("Passwords do not match. Please try again.");
    }
    String hashedPassword = passwordEncoder.encode(userDto.password());
    userRepository.save(AppUser.register(userDto.email(), hashedPassword, Role.MEMBER));
    log.info("AppUser with email {} registered successfully.", userDto.email());
  }

  @Transactional(readOnly = true)
  public UserResponse getAuthenticatedUser(Long userId) {
    AppUser appUser = getUserOrThrow(userId);
    return userMapper.toUserResponse(appUser);
  }

  @Transactional
  public ResponseMessage updatePassword(Long userId, UpdatePasswordRequest passwordDTO) {
    AppUser appUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    if (!passwordEncoder.matches(passwordDTO.oldPassword(), appUser.getHashedPassword())
        || !passwordDTO.newPassword()
        .equals(passwordDTO.confirmNewPassword())) {
      throw new PasswordMisMatchException("Passwords do not match. Please try again.");
    }
    String newHashedPw = passwordEncoder.encode(passwordDTO.newPassword());
    appUser.changePassword(newHashedPw);
    log.info("Password for appUser {} updated successfully.", userId);
    return new ResponseMessage("Password has been updated successfully.");
  }

  @Transactional
  public UserResponse updateContactInfo(Long userId, ContactInfoRequest request) {
    AppUser appUser = getUserOrThrow(userId);
    appUser.changeContactInfo(request.phoneNr());
    log.info("Contact info for appUser {} updated successfully.", userId);
    return userMapper.toUserResponse(appUser);
  }

  /**
   * Creates and adds a new address to the specific appUser.
   *
   * @param userId  ID of the appUser that is updating address
   * @param request {@link AddressRequest} Contains address fields with validation
   * @return {@link AddressResponse} The newly updated address
   */
  @Transactional
  public AddressResponse updateAddress(Long userId, AddressRequest request) {
    AppUser appUser = getUserOrThrow(userId);
    appUser.changeAddress(userMapper.toAddress(request));
    userRepository.save(appUser);
    log.info("Address for appUser {} updated successfully.", userId);
    return userMapper.toAddressResponse(appUser);
  }

  private AppUser getUserOrThrow(Long userId) {
    return userRepository.findById(userId).orElseThrow(() ->
        new NotFoundException(String.format("AppUser with id %d not found.", userId)));
  }
}
