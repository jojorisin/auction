package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.admin.AddItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemDTO;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.mapper.ItemMapper;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final AuctionRepository auctionRepository;

    public List<AdminItemDTO> findAllItems(Category category, Category.SubCategory subCategory) {
        List<Item> items = itemRepository.findAllItems(category, subCategory);
        return items.stream().map(itemMapper::toRecord).toList();
    }

    public AdminItemDTO findItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(NotFoundException::new);
        return itemMapper.toRecord(item);
    }

    public AdminItemDTO addItem(AddItemRequest request) {
        User seller = userRepository.findById(request.sellerId()).orElseThrow(UserNotFoundException::new);
        Item item = Item.create(
                seller,
                request.category(),
                request.subCategory(),
                request.title(),
                request.description(),
                request.valuation(),
                request.imageUrls()
        );
        itemRepository.save(item);
        return itemMapper.toRecord(item);
    }

    public AdminItemDTO updateItem(UpdateItemRequest dto, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(NotFoundException::new);

        if (dto.category() != null && dto.subCategory() != null) {
            item.updateCategories(dto.category(), dto.subCategory());
        }
        if (dto.title() != null) {
            item.updateTitle(dto.title());
        }
        if (dto.description() != null) {
            item.updateDescription(dto.description());
        }
        if (dto.valuation() != null) {
            item.updateValuation(dto.valuation());
        }
        if (dto.imageUrls() != null) {
            item.updateImageUrls(dto.imageUrls());
        }
        if (dto.imageUrl() != null) {
            item.addImage(dto.imageUrl());
        }

        itemRepository.save(item);
        return itemMapper.toRecord(item);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(NotFoundException::new);
        if (auctionRepository.existsByItemIdAndStatus(item.getId(), AuctionStatus.ACTIVE)) {
            log.warn("Deleting Item with id {} is currently at auction. Deleting item is not allowed.", itemId);
            throw new IllegalStateException(String.format("Item with id %d is currently at auction.", itemId));
        }
        itemRepository.delete(item);
    }
}
