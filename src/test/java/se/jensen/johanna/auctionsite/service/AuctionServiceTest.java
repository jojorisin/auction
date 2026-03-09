package se.jensen.johanna.auctionsite.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jensen.johanna.auctionsite.dto.admin.CreateAuctionRequest;
import se.jensen.johanna.auctionsite.mapper.AuctionMapper;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.util.AuctionTestBase;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest extends AuctionTestBase {

  @InjectMocks
  private AuctionService auctionService;
  @Mock
  private ItemRepository itemRepository;
  @Mock
  private AuctionRepository auctionRepository;
  @Mock
  private AuctionMapper auctionMapper;

  @Test
  void shouldCreateAuctionForItemAndSetItemStatusToPlanned() {
    when(itemRepository.findById(any())).thenReturn(Optional.of(item));

    auctionService.createAuctionForItem(item.getId(), new CreateAuctionRequest(10000));

    assertThat(item.getStatus()).isEqualTo(ItemStatus.PREPARED);
    verify(auctionRepository).save(any(Auction.class));
  }
}