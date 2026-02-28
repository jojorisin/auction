package se.jensen.johanna.auctionsite.dto;

import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.User;

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
        User seller,
        Integer orderSum,
        User buyer

) {
}
