package se.jensen.johanna.auctionsite.event;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import se.jensen.johanna.auctionsite.dto.BidHistoryDTO;
import se.jensen.johanna.auctionsite.service.BidService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BidEventListener {
    private final SimpMessagingTemplate template;
    private final BidService bidService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBidPlaced(BidPlacedEvent event) {
        List<BidHistoryDTO> bids = bidService.getBidsForActiveAuction(event.auctionId());
        template.convertAndSend("/topic/bids/" + event.auctionId(), bids);
    }
}
