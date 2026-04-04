package se.jensen.johanna.auctionsite.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.AuctionResponse;
import se.jensen.johanna.auctionsite.dto.AuctionsListResponse;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateAuctionRequest;
import se.jensen.johanna.auctionsite.dto.admin.FailedToLaunch;
import se.jensen.johanna.auctionsite.dto.admin.LaunchInstants;
import se.jensen.johanna.auctionsite.dto.admin.LaunchRequest;
import se.jensen.johanna.auctionsite.dto.admin.LaunchResponse;
import se.jensen.johanna.auctionsite.dto.admin.UpdateAuctionRequest;
import se.jensen.johanna.auctionsite.dto.my.WonAuctionResponse;
import se.jensen.johanna.auctionsite.exception.DomainArgumentException;
import se.jensen.johanna.auctionsite.exception.DomainStateException;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.AuctionMapper;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.repository.OrderRepository;
import se.jensen.johanna.auctionsite.util.TimeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

  private final AuctionRepository auctionRepository;
  private final AuctionMapper auctionMapper;
  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;

  //       *****************ADMIN***********

  /**
   * Retrieves all auctions in the system for administrative purposes.
   *
   * @return a list of {@link AdminAuctionResponse} containing all auctions.
   */
  @Transactional(readOnly = true)
  public Page<AdminAuctionResponse> getAllAuctions(Category category,
      Category.SubCategory subCategory, AuctionStatus status, Pageable pageable) {
    return auctionRepository.findAllAuctions(category, subCategory, status, pageable)
        .map(auctionMapper::toAdminAuctionResponse);
  }

  /**
   * Retrieves a specific auction by its ID for administrative purposes.
   *
   * @return {@link AdminAuctionResponse} detailed information for admin
   */
  @Transactional(readOnly = true)
  public AdminAuctionResponse getAuction(Long auctionId) {
    Auction auction = getAuctionOrThrow(auctionId);
    return auctionMapper.toAdminAuctionResponse(auction);
  }

  /**
   * Creates and saves a new auction for a specific item.
   *
   * @param request the request containing details for auction
   * @return the {@link AdminAuctionResponse} of the newly created auction.
   * @throws DomainStateException    if the item is not available or is missing required fields
   * @throws DomainArgumentException if the accepted price is higher than the item's valuation
   */
  @Transactional
  public AdminAuctionResponse createAuctionForItem(Long itemId, CreateAuctionRequest request) {
    Item item = getItemOrThrow(itemId);
    if (item.getStatus() != ItemStatus.AVAILABLE) {
      throw new DomainStateException(
          String.format("Auction already exists or is planned for item with id %d", item.getId()));
    }
    Auction auction = Auction.createAuction(item, request.acceptedPrice());
    auctionRepository.save(auction);
    log.info("Auction created for item {}", item.getId());
    return auctionMapper.toAdminAuctionResponse(auction);
  }

  /**
   * Launches a batch of auctions
   *
   * @param request {@link LaunchRequest} request containing size, start and end time. Returns
   *                default value if null
   * @return {@link LaunchResponse} returns nr of successful and failed launches and a list with IDs
   * of failed auctions
   */
  @Transactional
  public LaunchResponse launchBatch(LaunchRequest request) {
    // convert to instants from local
    LaunchInstants instants = TimeUtils.getLaunchInstants(request);
    if (instants.endTime().isBefore(instants.startTime())) {
      throw new DomainArgumentException("End time must be after start time");
    }
    int size = request.size() != null ? request.size() : 50;
    Pageable limit = PageRequest.of(0, size);

    Page<Auction> auctionsToLaunch = auctionRepository.findByStatusOrderByCreatedAtAsc(
        request.status() != null ? request.status() : AuctionStatus.INACTIVE, limit);

    log.info("Launching {} auctions with status {} from {} to {}", size, request.status(),
        instants.startTime(), instants.endTime());

    int minutesToAdd = 0;
    int successfulLaunches = 0;
    int failedLaunches = 0;
    List<FailedToLaunch> failed = new ArrayList<>();
    for (Auction a : auctionsToLaunch) {
      if (!a.isReadyToLaunch()) {
        failed.add(new FailedToLaunch(a.getId(), "Invalid or missing fields"));
        failedLaunches++;
        log.warn("Failed to launch: Auction {} has invalid or missing fields", a.getId());
        continue;
      }
      Instant individualEndTime = instants.endTime().plus(minutesToAdd, ChronoUnit.MINUTES);
      a.launchAuction(instants.startTime(), individualEndTime);
      minutesToAdd++;
      successfulLaunches++;
    }
    auctionRepository.saveAll(auctionsToLaunch);
    log.info("Launched {} auctions, {} failed", successfulLaunches, failedLaunches);
    return new LaunchResponse(successfulLaunches, failedLaunches, failed);
  }


  /**
   * Updates an existing auction with new information.
   *
   * @return the {@link AdminAuctionResponse} of the updated auction.
   * @throws DomainStateException if the auction is active
   */
  @Transactional
  public AdminAuctionResponse updateAuction(Long auctionId, UpdateAuctionRequest request) {
    Auction auction = getAuctionOrThrow(auctionId);
    if (request.acceptedPrice() != null) {
      auction.updateAcceptedPrice(request.acceptedPrice());
    }
    auctionRepository.save(auction);
    log.info("Auction {} updated.", auction.getId());
    return auctionMapper.toAdminAuctionResponse(auction);
  }

  /**
   * Deletes an auction from the system.
   *
   * @throws DomainStateException if the auction is active
   */
  @Transactional
  public void deleteAuction(Long auctionId) {
    Auction auction = getAuctionOrThrow(auctionId);
    if (auction.getStatus() == AuctionStatus.ACTIVE) {
      throw new DomainStateException("Auction is active and can not be deleted.");
    }
    auctionRepository.delete(auction);
    log.info("Auction {} deleted.", auctionId);
  }

  //*****************PUBLIC**********

  /**
   * Retrieves an active auction with public details, including item info.
   */
  @Transactional(readOnly = true)
  public AuctionResponse getActiveAuction(Long auctionId) {
    Auction auction = getAuctionOrThrow(auctionId);
    return auctionMapper.toAuctionResponse(auction);
  }

  /**
   * Retrieves a list of all active auctions, optionally filtered and sorted. Containing less
   * detailed information for scrolling through auctions.
   */
  @Transactional(readOnly = true)
  public Page<AuctionsListResponse> getAllActiveAuctions(String query, AuctionStatus status,
      Category category,
      Category.SubCategory subCategory, Pageable pageable) {
    return auctionRepository.findFilteredAuctions(query, status, category, subCategory,
            pageable)
        .map(auctionMapper::toAuctionsList);
  }

  /**
   * Retrieves a list of all sold auctions where status is SOLD and the current appUser has the
   * winning bid
   */
  @Transactional(readOnly = true)
  public List<WonAuctionResponse> getMyWonAuctions(Long userId) {
    return orderRepository.findByBuyer_id(userId).stream().map(auctionMapper::toMyWonAuction)
        .toList();
  }

  private Auction getAuctionOrThrow(Long auctionId) {
    return auctionRepository.findById(auctionId).orElseThrow(
        () -> new NotFoundException(String.format("Auction with id %d not found", auctionId)));
  }

  private Item getItemOrThrow(Long itemId) {
    return itemRepository.findById(itemId).orElseThrow(
        () -> new NotFoundException(String.format("Item with id %d not found", itemId)));
  }
}

