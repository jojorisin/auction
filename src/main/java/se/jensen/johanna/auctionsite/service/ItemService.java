package se.jensen.johanna.auctionsite.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.dto.admin.AddItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemDTO;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.exception.NotFoundException;
import se.jensen.johanna.auctionsite.exception.UserNotFoundException;
import se.jensen.johanna.auctionsite.mapper.ItemMapper;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.User;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.repository.ItemRepository;
import se.jensen.johanna.auctionsite.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

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
        item.update(
                dto.category(),
                dto.subCategory(),
                dto.title(),
                dto.description(),
                dto.valuation(),
                dto.imageUrls()
        );
        itemRepository.save(item);
        return itemMapper.toRecord(item);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(NotFoundException::new);
        itemRepository.delete(item);
    }
}
