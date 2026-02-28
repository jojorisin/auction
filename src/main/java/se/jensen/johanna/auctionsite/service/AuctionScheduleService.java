package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionScheduleService {

    private final AuctionRepository auctionRepository;
    private final AuctionClosingService closingService;

    @Scheduled(fixedRate = 600000)
    public void checkEndedAuctions() {
        List<Auction> endedAuctions = auctionRepository.findEndedAuctionsWithBidsAndItemSeller(Instant.now());

        if (endedAuctions.isEmpty()) {
            return;
        }

        for (Auction a : endedAuctions) {
            try {
                closingService.closeAuction(a);
            } catch (Exception e) {
                log.error("Error closing auction {}: {}", a.getId(), e.getMessage());
            }
        }
        log.info("Closed {} ended auctions.", endedAuctions.size());
    }
}
