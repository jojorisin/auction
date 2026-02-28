package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.AuctionResponse;
import se.jensen.johanna.auctionsite.dto.AuctionsListResponse;
import se.jensen.johanna.auctionsite.dto.admin.*;
import se.jensen.johanna.auctionsite.dto.my.WonAuctionResponse;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.AuctionMapper;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final ItemRepository itemRepository;

    //       *****************ADMIN***********

    /**
     * Retrieves all auctions in the system for administrative purposes.
     *
     * @return a list of {@link AdminAuctionResponse} containing all auctions.
     */
    public Page<AdminAuctionResponse> findAllAuctions(
            Category category,
            Category.SubCategory subCategory,
            Pageable pageable
    ) {
        return auctionRepository.findAllAuctions(category, subCategory, pageable)
                                .map(auctionMapper::toAdminAuctionResponse);
    }

    /**
     * Retrieves a specific auction by its ID for administrative purposes.
     *
     * @param auctionId the ID of the auction to retrieve.
     * @return the {@link AdminAuctionResponse} for the specified auction.
     * @throws NotFoundException if no auction is found with the given ID.
     */
    public AdminAuctionResponse getAuction(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);
        return auctionMapper.toAdminAuctionResponse(auction);
    }

    /**
     * Creates and saves a new auction linked to a specific item.
     *
     * @param request the data transfer object containing auction details and item ID.
     * @return the {@link AdminAuctionResponse} of the newly created auction.
     * @throws NotFoundException if the item associated with the auction does not exist.
     */
    @Transactional
    public AdminAuctionResponse createAuctionForItem(CreateAuctionRequest request) {
        Item item = itemRepository.findById(request.itemId()).orElseThrow(() ->
                new NotFoundException(String.format("Item with id %d not found", request.itemId()))
        );

        if (item.getStatus() != ItemStatus.INACTIVE) {
            throw new IllegalStateException(String.format(
                    "Auction already exists or is planned for item with id %d",
                    item.getId()
            ));
        }
        Auction auction = Auction.prepareAuction(item, request.acceptedPrice());
        auctionRepository.save(auction);
        log.info("Auction created for item {}", item.getId());
        return auctionMapper.toAdminAuctionResponse(auction);
    }

    /**
     * Launches a batch of auctions
     *
     * @param request {@link LaunchBatchRequest} request containing size, start and end time. Returns default value if null
     * @return {@link LaunchBatchResponse} returns nr of successful and failed launches and a list with IDs of failed auctions
     */
    @Transactional
    public LaunchBatchResponse launchBatch(LaunchBatchRequest request) {
        // use getters to return default values if null
        Instant startTime = request.getStartTime();
        Instant endTime = request.getEndTime();
        Pageable limit = PageRequest.of(0, request.getSize());

        Page<Auction> auctionsToLaunch = auctionRepository.findByStatusOrderByCreatedAtAsc(
                AuctionStatus.INACTIVE,
                limit
        );

        int minutesToAdd = 0;
        int successfulLaunches = 0;
        int failedLaunches = 0;
        List<FailedToLaunch> failed = new ArrayList<>();
        for (Auction a : auctionsToLaunch) {
            if (!a.isReadyToLaunch()) {
                failed.add(new FailedToLaunch(a.getId(), "Missing required fields"));
                failedLaunches++;
                log.warn("Failed to launch: Auction {} is missing required fields", a.getId());
                continue;
            }
            if (a.getItem().getStatus() != ItemStatus.INACTIVE) {
                failed.add(new FailedToLaunch(a.getId(), "Auction already exists for item"));
                failedLaunches++;
                log.warn(
                        "Failed to launch: Item {} already has an active or planned auction", a.getItem().getId()
                );
                continue;
            }
            Instant individualEndTime = endTime.plus(minutesToAdd, ChronoUnit.MINUTES);
            a.launchAuction(startTime, individualEndTime);
            minutesToAdd++;
            successfulLaunches++;
        }
        auctionRepository.saveAll(auctionsToLaunch);
        log.info("Launched {} auctions, {} failed", successfulLaunches, failedLaunches);
        return new LaunchBatchResponse(successfulLaunches, failedLaunches, failed);
    }

    /**
     * Launches one single auction.
     *
     * @param auctionId ID of auction to put public
     * @param request   containing start and endtime.
     * @return {@link ManualLaunchResponse} containing detailed info about the auction
     */
    @Transactional
    public ManualLaunchResponse manualLaunch(Long auctionId, ManualLaunchRequest request) {
        // user getters to return default values if null
        Instant startTime = request.getStartTime();
        Instant endTime = request.getEndTime();

        Auction auction = getAuctionOrThrow(auctionId);
        if (auction.getItem().getStatus() != ItemStatus.INACTIVE) {
            throw new IllegalStateException(String.format(
                    "Auction already exists for item with id %d",
                    auction.getItem().getId()
            ));
        }
        auction.launchAuction(startTime, endTime);
        auctionRepository.save(auction);
        String typeOfLaunch = startTime.isBefore(Instant.now()) ? "Planned" : "Launched";
        log.info(
                "Auction {} manually {}. start time: {}, end time: {}",
                auction.getId(),
                typeOfLaunch,
                startTime,
                endTime
        );
        return auctionMapper.toManualLaunchResponse(auction);
    }

    /**
     * Updates an existing auction with new information.
     *
     * @param request   the data transfer object containing the updated details.
     * @param auctionId the ID of the auction to update.
     * @return the {@link AdminAuctionResponse} of the updated auction.
     * @throws NotFoundException if no auction is found with the given ID.
     */
    @Transactional
    public AdminAuctionResponse updateAuction(Long auctionId, UpdateAuctionRequest request) {
        Auction auction = getAuctionOrThrow(auctionId);
        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            throw new IllegalStateException(String.format(
                    "Auction with id %d is active and can not be updated",
                    auctionId
            ));
        }

        if (request.acceptedPrice() != null) {
            auction.updateAcceptedPrice(request.acceptedPrice());
        }
        if (request.itemId() != null) {
            Item item = itemRepository.findById(request.itemId()).orElseThrow(() ->
                    new NotFoundException(String.format("Item with id %d not found", request.itemId()))
            );
            auction.updateItem(item);
        }
        auctionRepository.save(auction);
        log.info("Auction {} updated.", auction.getId());
        return auctionMapper.toAdminAuctionResponse(auction);
    }

    /**
     * Deletes an auction from the system.
     *
     * @param auctionId the ID of the auction to delete.
     * @throws NotFoundException if no auction is found with the given ID.
     */
    @Transactional
    public void deleteAuction(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);
        auctionRepository.delete(auction);
        log.info("Auction {} deleted.", auctionId);
    }

    //*****************PUBLIC**********

    /**
     * Retrieves an active auction with public details, including item info and bid history.
     *
     * @param auctionId the ID of the auction to retrieve.
     * @return the {@link AuctionResponse} containing auction, item, and bid information.
     * @throws NotFoundException if no auction is found with the given ID.
     */
    public AuctionResponse getActiveAuction(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);
        return auctionMapper.toAuctionResponse(auction);
    }

    /**
     * Retrieves a list of all active auctions, optionally filtered by category and sorted.
     * Containing less detailed information for scrolling through auctions.
     *
     * @param category    the category to filter by (optional).
     * @param subCategory the subcategory to filter by (optional).
     * @return a list of {@link AuctionsListResponse} representing the active auctions.
     */
    public Page<AuctionsListResponse> getAllActiveAuctions(
            Category category,
            Category.SubCategory subCategory,
            Pageable pageable
    ) {
        return auctionRepository.findActiveAuctions(category, subCategory, pageable).map(auctionMapper::toAuctionsList);
    }

    /**
     * Retrieves a list of all auctions where status is SOLD, and the current user has the highest bid
     *
     * @param userId ID of user to fetch won auctions for
     * @return List of won auctions
     */
    public List<WonAuctionResponse> getMyWonAuctions(Long userId) {
        return auctionRepository.findWonAuctionsByUserId(userId).stream().map(auctionMapper::toMyWonAuction).toList();
    }

    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findById(auctionId).orElseThrow(() ->
                new NotFoundException(String.format("Auction with id %d not found", auctionId))
        );
    }
}

