package se.jensen.johanna.auctionsite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.auctionsite.dto.enums.BidStatus;
import se.jensen.johanna.auctionsite.dto.my.MyActiveBids;
import se.jensen.johanna.auctionsite.model.Bid;

@Mapper(componentModel = "spring")
public interface BidMapper {

    @Mapping(target = "auctionId", source = "bid.auction.id")
    @Mapping(target = "title", source = "bid.auction.item.title")
    @Mapping(target = "imageUrls", source = "bid.auction.item.imageUrls")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "highestBid", expression = "java(bid.getAuction().leadingAmount())")
    @Mapping(target = "endTime", source = "bid.auction.endTime")
    @Mapping(target = "maxSum", source = "maxSum")
    MyActiveBids toMyActiveBids(Bid bid, BidStatus status, Integer maxSum);
}
