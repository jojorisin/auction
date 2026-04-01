package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.my.AddressRequest;
import se.jensen.johanna.auctionsite.dto.my.AddressResponse;
import se.jensen.johanna.auctionsite.dto.my.UserResponse;
import se.jensen.johanna.auctionsite.model.Address;
import se.jensen.johanna.auctionsite.model.AppUser;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "firstName", source = "address.firstName")
  @Mapping(target = "lastName", source = "address.lastName")
  @Mapping(target = "co", source = "address.co")
  @Mapping(target = "streetName", source = "address.streetName")
  @Mapping(target = "streetName2", source = "address.streetName2")
  @Mapping(target = "postalCode", source = "address.postalCode")
  @Mapping(target = "city", source = "address.city")
  @Mapping(target = "country", source = "address.country")
  AddressResponse toAddressResponse(AppUser appUser);

  @Mapping(target = "address", source = "appUser")
  @Mapping(target = "userId", source = "id")
  UserResponse toUserResponse(AppUser appUser);

  default Address toAddress(AddressRequest request) {
    return Address.create(
        request.firstName(),
        request.lastName(),
        request.co(),
        request.streetName(),
        request.streetName2(),
        request.postalCode(),
        request.city(),
        request.country()
    );
  }
}

