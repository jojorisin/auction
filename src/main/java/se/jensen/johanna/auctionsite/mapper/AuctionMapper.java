package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import se.jensen.johanna.auctionsite.dto.AuctionResponse;
import se.jensen.johanna.auctionsite.dto.AuctionsListResponse;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.admin.ManualLaunchResponse;
import se.jensen.johanna.auctionsite.dto.my.WonAuctionResponse;
import se.jensen.johanna.auctionsite.model.Auction;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ItemMapper.class, BidMapper.class})
public interface AuctionMapper {

    @Mapping(target = "auctionId", source = "id")
    @Mapping(target = "imageUrls", source = "auction.item.imageUrls")
    @Mapping(target = "title", source = "auction.item.title")
    @Mapping(target = "valuation", source = "auction.item.valuation")
    @Mapping(target = "highestBid", expression = "java(auction.leadingAmount())")
    AuctionsListResponse toAuctionsList(Auction auction);

    @Mapping(target = "itemResponse", source = "auction.item")
    AuctionResponse toAuctionResponse(Auction auction);

    @Mapping(target = "auctionId", source = "id")
    @Mapping(target = "highestBid", expression = "java(auction.leadingAmount())")
    @Mapping(target = "title", source = "auction.item.title")
    WonAuctionResponse toMyWonAuction(Auction auction);

    @Mapping(target = "auctionId", source = "auction.id")
    @Mapping(target = "adminItemResponse", source = "auction.item")
    ManualLaunchResponse toManualLaunchResponse(Auction auction);

    @Mapping(target = "auctionId", source = "id")
    @Mapping(target = "adminItemResponse", source = "item")
    AdminAuctionResponse toAdminAuctionResponse(Auction auction);
}
