package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import se.jensen.johanna.auctionsite.dto.AuctionDTO;
import se.jensen.johanna.auctionsite.dto.AuctionsListDTO;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.admin.ManualLaunchResponse;
import se.jensen.johanna.auctionsite.dto.my.MyWonAuctionDTO;
import se.jensen.johanna.auctionsite.model.Auction;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ItemMapper.class, BidMapper.class})
public interface AuctionMapper {


    //***********PUBLIC MAPPERS******

    @Mapping(target = "auctionId", source = "id")
    @Mapping(target = "imageUrls", source = "auction.item.imageUrls")
    @Mapping(target = "title", source = "auction.item.title")
    @Mapping(target = "valuation", source = "auction.item.valuation")
    @Mapping(target = "highestBid", expression = "java(auction.leadingAmount())")
    AuctionsListDTO toAuctionsList(Auction auction);


    //***********MY MAPPERS*************

    @Mapping(target = "auctionId", source = "id")
    @Mapping(target = "highestBid", expression = "java(auction.leadingAmount())")
    @Mapping(target = "title", source = "auction.item.title")
    MyWonAuctionDTO toMyWonAuction(Auction auction);

    //************ADMIN MAPPERS**********

    //Auction toEntity(CreateAuctionRequest dto);


    @Mapping(target = "itemDTO", source = "auction.item")
    AuctionDTO toAuctionDTO(Auction auction);

    @Mapping(target = "auctionId", source = "auction.id")
    @Mapping(target = "adminItemDTO", source = "auction.item")
    ManualLaunchResponse toManualLaunchResponse(Auction auction);


    @Mapping(target = "auctionId", source = "id")
    @Mapping(target = "adminItemDTO", source = "item")
    AdminAuctionResponse toAdminRecord(Auction auction);
}
