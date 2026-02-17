package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.ItemDTO;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemDTO;
import se.jensen.johanna.auctionsite.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {


    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "itemId", source = "id")
    AdminItemDTO toRecord(Item item);

    ItemDTO toShowRecord(Item item);


}
