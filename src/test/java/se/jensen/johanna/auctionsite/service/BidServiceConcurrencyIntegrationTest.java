package se.jensen.johanna.auctionsite.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import se.jensen.johanna.auctionsite.dto.BidRequest;
import se.jensen.johanna.auctionsite.exception.InvalidBidException;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;
import se.jensen.johanna.auctionsite.util.AuctionTestBase;
import se.jensen.johanna.auctionsite.util.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
public class BidServiceConcurrencyIntegrationTest extends AuctionTestBase {

  @Autowired
  private BidService bidService;

  @Autowired
  private AuctionRepository auctionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Test
  void shouldHandleConcurrentBids() throws InterruptedException {
    User seller = userRepository.save(TestDataFactory.createUser("seller@test.com"));
    User bidder1 = userRepository.save(TestDataFactory.createUser("u1@u.com"));
    User bidder2 = userRepository.save(TestDataFactory.createUser("u2@u.com"));
    Item savedItem = itemRepository.save(TestDataFactory.createItem(seller));
    Auction savedAuction = auctionRepository.save(
        TestDataFactory.createActiveAuction(null, savedItem, 3000));

    int threadCount = 2;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(1);
    List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
    for (int i = 0; i < threadCount; i++) {
      final long bidderId = i == 0 ? bidder1.getId() : bidder2.getId();
      executor.submit(() -> {
        try {
          latch.await();
          bidService.placeBid(new BidRequest(1000), bidderId, savedAuction.getId());
        } catch (InvalidBidException e) {
          // expected

        } catch (Exception e) {
          errors.add(e);
          e.printStackTrace();
        }
      });
    }

    latch.countDown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertTrue(errors.stream().noneMatch(e -> e instanceof OptimisticLockingFailureException),
        "OptimisticLockingFailureException should be handled by retry");
  }
}
