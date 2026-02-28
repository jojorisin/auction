package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.admin.AddItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.ItemMapper;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.model.enums.ItemStatus;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final AuctionRepository auctionRepository;

    @Transactional(readOnly = true)
    public List<AdminItemResponse> findAllItems(Category category, Category.SubCategory subCategory) {
        List<Item> items = itemRepository.findAllItems(category, subCategory);
        return items.stream().map(itemMapper::toRecord).toList();
    }

    @Transactional(readOnly = true)
    public AdminItemResponse findItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id %d not found.", itemId))
        );
        return itemMapper.toRecord(item);
    }

    @Transactional
    public AdminItemResponse addItem(AddItemRequest request) {
        User seller = userRepository.findById(request.sellerId()).orElseThrow(() ->
                new NotFoundException(String.format(
                        "Seller with id %d not found when creating item.",
                        request.sellerId()
                )));
        Item item = itemMapper.toItem(request, seller);
        itemRepository.save(item);
        log.info("Item {} created for seller {}", item.getId(), item.getSeller().getId());
        return itemMapper.toRecord(item);
    }

    @Transactional
    public AdminItemResponse updateItem(Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id %d not found.", itemId)));

        if (request.category() != null && request.subCategory() != null) {
            item.updateCategories(request.category(), request.subCategory());
        }
        if (request.title() != null) {
            item.updateTitle(request.title());
        }
        if (request.description() != null) {
            item.updateDescription(request.description());
        }
        if (request.imageUrls() != null) {
            item.updateImageUrls(request.imageUrls());
        }
        if (request.imageUrl() != null) {
            item.addImage(request.imageUrl());
        }
        if (request.valuation() != null) {
            if (item.getStatus() != ItemStatus.INACTIVE) {
                throw new IllegalStateException(String.format(
                        "Item with id %d is currently at auction and valuation can not be updated.",
                        itemId
                ));
            }
            item.updateValuation(request.valuation());
        }

        itemRepository.save(item);
        log.info("Item {} updated.", item.getId());
        return itemMapper.toRecord(item);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id %d not found", itemId)));
        if (auctionRepository.existsByItemIdAndStatus(item.getId(), AuctionStatus.ACTIVE)) {
            throw new IllegalStateException(String.format(
                    "Item with id %d is currently at auction and can not be deleted.",
                    itemId
            ));
        }
        itemRepository.delete(item);
        log.info("Item {} deleted.", itemId);
    }
}
