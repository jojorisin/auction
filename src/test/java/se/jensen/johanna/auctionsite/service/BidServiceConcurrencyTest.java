package se.jensen.johanna.auctionsite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.util.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
public class BidServiceConcurrencyTest {
    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        User bidder1 = TestDataFactory.createUser(1L);
        User bidder2 = TestDataFactory.createUser(2L);
    }

    @Test
    void shouldRetryOnOptimisticLockingException() throws InterruptedException {

    }
}
