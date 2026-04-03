package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import se.jensen.johanna.auctionsite.dto.AuctionResponse;
import se.jensen.johanna.auctionsite.dto.AuctionsListResponse;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.my.WonAuctionResponse;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Order;
import se.jensen.johanna.auctionsite.service.enums.BidTier;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ItemMapper.class, BidMapper.class}, imports = BidTier.class)
public interface AuctionMapper {

  @Mapping(target = "auctionId", source = "id")
  @Mapping(target = "imageUrls", source = "auction.item.imageUrls")
  @Mapping(target = "title", source = "auction.item.title")
  @Mapping(target = "valuation", source = "auction.item.valuation")
  @Mapping(target = "highestBid", expression = "java(auction.leadingAmount())")
  AuctionsListResponse toAuctionsList(Auction auction);

  @Mapping(target = "auctionId", source = "id")
  @Mapping(target = "itemResponse", source = "auction.item")
  @Mapping(target = "increment", expression = "java(BidTier.getBidIncrement(auction.getItem().getValuation()))")
  AuctionResponse toAuctionResponse(Auction auction);

  @Mapping(target = "auctionId", source = "order.auction.id")
  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "winningBid", source = "orderSum")
  @Mapping(target = "endTime", source = "auction.endTime")
  @Mapping(target = "title", source = "auction.item.title")
  @Mapping(target = "imageUrls", source = "auction.item.imageUrls")
  WonAuctionResponse toMyWonAuction(Order order);


  @Mapping(target = "auctionId", source = "id")
  @Mapping(target = "adminItemResponse", source = "item")
  AdminAuctionResponse toAdminAuctionResponse(Auction auction);
}
