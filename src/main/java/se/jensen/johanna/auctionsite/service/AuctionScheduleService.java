package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.EmailTypeDTO;
import se.jensen.johanna.auctionsite.dto.enums.BidStatus;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionScheduleService {

    private final AuctionRepository auctionRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 600000)
    public void checkEndedAuctions() {
        List<Auction> soldAuctions = auctionRepository.findEndedAuctionsWithBid(Instant.now());

        for (Auction a : soldAuctions) {
            a.getWinningBid().ifPresentOrElse(
                    bid -> {
                        if (bid.getBidSum() >= a.getAcceptedPrice()) {
                            a.closeSoldAuction(bid);
                            EmailTypeDTO emailTypeDTO = new EmailTypeDTO(
                                    bid.getBidder().getEmail(),
                                    BidStatus.WON, a.getId(), null,
                                    a.getItem().getTitle()
                            );
                            emailService.sendEmail(emailTypeDTO);

                        } else {
                            a.closeAuctionAcceptedNotMet();
                        }
                        //mejl till alla i budlistan? notifyallparticipants

                    }, a::closeExpiredAuction
            );


        }

    }

    @Scheduled(cron = "0 * * * * *")
    public void checkReminder() {

    }


}
