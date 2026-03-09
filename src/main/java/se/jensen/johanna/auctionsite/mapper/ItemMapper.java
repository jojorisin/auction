package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.ItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateItemRequest;
import se.jensen.johanna.auctionsite.dto.my.MyItemResponse;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;

@Mapper(componentModel = "spring")
public interface ItemMapper {

  @Mapping(target = "sellerId", source = "seller.id")
  @Mapping(target = "itemId", source = "id")
  AdminItemResponse toRecord(Item item);

  ItemResponse toItemResponse(Item item);

  @Mapping(target = "sellerId", source = "seller.id")
  MyItemResponse toMyItemResponse(Item item);

  default Item toItem(CreateItemRequest request, User seller) {
    return Item.create(
        seller,
        request.category(),
        request.subCategory(),
        request.title(),
        request.description(),
        request.valuation(),
        request.imageUrls()
    );
  }
}
