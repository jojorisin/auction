package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.my.AddressResponse;
import se.jensen.johanna.auctionsite.dto.my.AppUserDTO;
import se.jensen.johanna.auctionsite.model.User;

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
    AddressResponse toAddressResponse(User user);

    @Mapping(target = "address", source = "user")
    AppUserDTO toAppUserDTO(User user);


}

