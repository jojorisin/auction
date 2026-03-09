package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.OrderResponse;
import se.jensen.johanna.auctionsite.model.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "auctionId", source = "auction.id")
  @Mapping(target = "title", source = "auction.item.title")
  @Mapping(target = "imageUrl", source = "auction.item.primaryImageUrl")
  OrderResponse toOrderResponse(Order order);

}
