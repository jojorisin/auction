package se.jensen.johanna.auctionsite.service.enums;

import lombok.Getter;

@Getter
public enum BidTier {
    TIER_1(0, 999, 50),
    TIER_2(1000, 4999, 100),
    TIER_3(5000, 9999, 200),
    TIER_4(10000, 19999, 500),
    TIER_5(20000, 99999, 1000),
    TIER_6(100000, 499999, 2000);

    private final int minVal;
    private final int maxVal;
    private final int increment;

    BidTier(int minVal, int maxVal, int increment) {
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.increment = increment;

    }

    public static int getBidIncrement(int valuation) {
        for (BidTier tier : values()) {
            if (valuation >= tier.minVal && valuation <= tier.maxVal) {
                return tier.increment;
            }

        }
        throw new IllegalArgumentException(String.format("No matching bid tier for valuation: %d", valuation));
    }


}
