package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.AuctionDTO;
import se.jensen.johanna.auctionsite.dto.AuctionsListDTO;
import se.jensen.johanna.auctionsite.dto.ManualLaunchRequest;
import se.jensen.johanna.auctionsite.dto.admin.*;
import se.jensen.johanna.auctionsite.dto.my.MyWonAuctionDTO;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.AuctionMapper;
import se.jensen.johanna.auctionsite.model.Auction;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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

    public List<AdminAuctionResponse> findAllAuctions() {
        return auctionRepository.findAll()
                                .stream().map(auctionMapper::toAdminRecord).toList();

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
        return auctionMapper.toAdminRecord(auction);


    }

    /**
     * Creates and saves a new auction linked to a specific item.
     *
     * @param dto the data transfer object containing auction details and item ID.
     * @return the {@link AdminAuctionResponse} of the newly created auction.
     * @throws NotFoundException if the item associated with the auction does not exist.
     */
    public AdminAuctionResponse createAuctionForItem(CreateAuctionRequest dto) {
        Item item = itemRepository.findById(dto.itemId()).orElseThrow(NotFoundException::new);
        if (auctionRepository.existsByItemIdAndStatusActiveOrInactive(item.getId())) {
            throw new IllegalStateException(String.format(
                    "Auction already exists for for item with id %d",
                    item.getId()
            ));
        }
        Auction auction = Auction.prepareAuction(item, dto.acceptedPrice());
        auctionRepository.save(auction);
        return auctionMapper.toAdminRecord(auction);


    }

    /**
     * Launches a large batch of auctions
     *
     * @param size      How many auctions to launch
     * @param startTime the start time for auctions
     * @param endTime   end time for auctions
     * @return {@link LaunchBatchResponse} returns how many were successful and failed
     */
    public LaunchBatchResponse launchBatch(int size, Instant startTime, Instant endTime) {
        startTime = startTime != null ? startTime : Instant.now();
        Pageable limit = PageRequest.of(0, size);
        endTime = endTime != null ? endTime : startTime.plus(7, ChronoUnit.DAYS);

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
                continue;
            }
            Instant individualEndTime = endTime.plus(minutesToAdd, ChronoUnit.MINUTES);
            a.launchAuction(startTime, individualEndTime);
            minutesToAdd++;
            successfulLaunches++;

        }
        auctionRepository.saveAll(auctionsToLaunch);

        return new LaunchBatchResponse(successfulLaunches, failedLaunches, failed);
    }

    public ManualLaunchResponse manualLaunch(Long auctionId, ManualLaunchRequest request) {
        Instant startTime = request.startTime() != null ? request.startTime() : Instant.now();
        Instant endTime = request.endTime() != null ? request.endTime() : startTime.plus(7, ChronoUnit.DAYS);


        Auction auction = getAuctionOrThrow(auctionId);
        if (auctionRepository.existsByItemIdAndStatusActiveOrInactive(auction.getItem().getId())) {
            throw new IllegalStateException(String.format(
                    "Auction already exists for for item with id %d",
                    auction.getItem().getId()
            ));
        }
        auction.launchAuction(startTime, endTime);
        auctionRepository.save(auction);
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

    public AdminAuctionResponse updateAuction(Long auctionId, UpdateAuctionRequest request) {
        Auction auction = getAuctionOrThrow(auctionId);

        if (request.acceptedPrice() != null) {
            auction.updateAcceptedPrice(request.acceptedPrice());
        }
        if (request.status() != null) {
            auction.updateStatus(request.status());
        }
        if (request.itemId() != null) {
            Item item = itemRepository.findById(request.itemId()).orElseThrow(NotFoundException::new);
            auction.updateItem(item);
        }

        auctionRepository.save(auction);
        return auctionMapper.toAdminRecord(auction);


    }

    /**
     * Deletes an auction from the system.
     *
     * @param auctionId the ID of the auction to delete.
     * @throws NotFoundException if no auction is found with the given ID.
     */

    public void deleteAuction(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);
        auctionRepository.delete(auction);
    }


    //*****************PUBLIC**********


    /**
     * Retrieves an active auction with public details, including item info and bid history.
     *
     * @param auctionId the ID of the auction to retrieve.
     * @return the {@link AuctionDTO} containing auction, item, and bid information.
     * @throws NotFoundException if no auction is found with the given ID.
     */

    public AuctionDTO getActiveAuction(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);
        return auctionMapper.toAuctionDTO(auction);

    }

    /**
     * Retrieves a list of all active auctions, optionally filtered by category and sorted.
     * Containing less detailed information for scrolling through auctions.
     *
     * @param category    the category to filter by (optional).
     * @param subCategory the subcategory to filter by (optional).
     * @return a list of {@link AuctionsListDTO} representing the active auctions.
     */

    public Page<AuctionsListDTO> getAllActiveAuctions(
            Category category,
            Category.SubCategory subCategory,
            Pageable pageable
    ) {


        return auctionRepository.findActiveAuctions(category, subCategory, pageable)
                                .map(auctionMapper::toAuctionsList);


    }


    /**
     * Retrieves a list of all auctions where status is SOLD, and the current user has the highest bid
     *
     * @param userId ID of user to fetch won auctions for
     * @return List of won auctions
     */
    public List<MyWonAuctionDTO> getMyWonAuctions(Long userId) {

        return auctionRepository.findWonAuctionsByUserId(userId).stream()
                                .map(auctionMapper::toMyWonAuction).toList();


    }


    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findById(auctionId).orElseThrow(() ->
                new NotFoundException(String.format("Auction with id %d not found", auctionId)));

    }


}

