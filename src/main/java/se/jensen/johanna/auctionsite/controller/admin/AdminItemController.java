package se.jensen.johanna.auctionsite.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.admin.AddItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemDTO;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.ItemService;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<AdminItemDTO>> getAllItems(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Category.SubCategory subCategory
    ) {
        return ResponseEntity.ok(itemService.findAllItems(category, subCategory));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<AdminItemDTO> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.findItem(itemId));
    }

    @PostMapping
    public ResponseEntity<AdminItemDTO> addItem(@RequestBody AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.addItem(request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<AdminItemDTO> editItem(@PathVariable Long itemId, @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(request, itemId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
