package se.jensen.johanna.auctionsite.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.dto.my.MyItemResponse;
import se.jensen.johanna.auctionsite.exception.DomainStateException;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.mapper.ItemMapper;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.repository.AuctionRepository;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final ItemMapper itemMapper;
  private final AuctionRepository auctionRepository;
  private final ImageService imageService;

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

  @SneakyThrows
  @Transactional
  public AdminItemResponse createItem(CreateItemRequest request) {
    User seller = userRepository.findById(request.sellerId()).orElseThrow(() ->
        new NotFoundException(String.format(
            "Seller with id %d not found.",
            request.sellerId()
        )));
    List<String> imageUrls =
        request.imageFiles() != null ? request.imageFiles().stream().map(imageService::uploadImage)
            .toList() : List.of();
    try {
      Item item = itemMapper.toItem(request, seller, imageUrls);
      itemRepository.save(item);
      log.info("Item {} created for seller {}", item.getId(), item.getSeller().getId());
      return itemMapper.toRecord(item);
    } catch (Exception e) {
      log.error("Error creating item: {}. Cleaning up {} uploaded images to S3", e.getMessage(),
          imageUrls.size());
      imageUrls.forEach(imageService::deleteImage);
      throw new RuntimeException("Error creating item", e);
    }
  }

  @SneakyThrows
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
    if (request.imageFiles() != null && !request.imageFiles().get(0).isEmpty()) {
      List<String> imageUrls = request.imageFiles().stream().map(imageService::uploadImage)
          .toList();
      item.addImageUrls(imageUrls);
    }
    if (request.valuation() != null) {
      item.updateValuation(request.valuation());
    }

    itemRepository.save(item);
    log.info("Item {} updated.", item.getId());
    return itemMapper.toRecord(item);
  }

  public List<MyItemResponse> getAllItemsForSeller(Long userId) {
    return itemRepository.findAllBySellerId(userId).stream().map(itemMapper::toMyItemResponse)
        .toList();
  }

  public MyItemResponse getItemForSeller(Long userId, Long itemId) {
    return itemRepository.findByIdAndSellerId(itemId, userId).map(itemMapper::toMyItemResponse)
        .orElseThrow(() -> new NotFoundException("Item not found."));
  }


  @Transactional
  public void deleteItem(Long itemId) {
    Item item = itemRepository.findById(itemId).orElseThrow(() ->
        new NotFoundException(String.format("Item with id %d not found", itemId)));
    if (auctionRepository.existsByItemIdAndStatus(item.getId(), AuctionStatus.ACTIVE)) {
      throw new DomainStateException(String.format(
          "Item with id %d is currently at auction and can not be deleted.",
          itemId
      ));
    }
    itemRepository.delete(item);
    log.info("Item {} deleted.", itemId);
  }
}
