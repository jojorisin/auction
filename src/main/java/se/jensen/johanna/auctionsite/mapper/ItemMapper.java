package se.jensen.johanna.auctionsite.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.ItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateItemRequest;
import se.jensen.johanna.auctionsite.dto.my.MyItemResponse;
import se.jensen.johanna.auctionsite.model.AppUser;
import se.jensen.johanna.auctionsite.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

  @Mapping(target = "sellerId", source = "seller.id")
  @Mapping(target = "itemId", source = "id")
  AdminItemResponse toRecord(Item item);

  ItemResponse toItemResponse(Item item);

  @Mapping(target = "sellerId", source = "seller.id")
  MyItemResponse toMyItemResponse(Item item);

  default Item toItem(CreateItemRequest request, AppUser seller, List<String> imageUrls) {
    return Item.create(
        seller,
        request.category(),
        request.subCategory(),
        request.title(),
        request.description(),
        request.valuation(),
        imageUrls
    );
  }
}
