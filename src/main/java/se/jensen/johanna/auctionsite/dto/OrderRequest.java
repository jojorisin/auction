package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.model.AppUser;
import se.jensen.johanna.auctionsite.model.Auction;

/**
 * Contains details about sold auction order
 *
 * @param auction
 * @param seller
 * @param orderSum
 * @param buyer
 */
public record OrderRequest(
    Auction auction,
    AppUser seller,
    Integer orderSum,
    AppUser buyer

) {

}
