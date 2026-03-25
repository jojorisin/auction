package se.jensen.johanna.auctionsite.controller.rest;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.ItemService;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

  private final ItemService itemService;

  @PostMapping
  public ResponseEntity<AdminItemResponse> createItem(
      @RequestBody @Valid CreateItemRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(request));
  }

  @GetMapping
  public ResponseEntity<List<AdminItemResponse>> getAllItems(
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Category.SubCategory subCategory) {
    return ResponseEntity.ok(itemService.findAllItems(category, subCategory));
  }

  @GetMapping("/{itemId}")
  public ResponseEntity<AdminItemResponse> getItem(@PathVariable Long itemId) {
    return ResponseEntity.ok(itemService.findItem(itemId));
  }

  @PutMapping("/{itemId}")
  public ResponseEntity<AdminItemResponse> updateItem(@PathVariable Long itemId,
      @RequestBody @Valid UpdateItemRequest request) {
    return ResponseEntity.ok(itemService.updateItem(itemId, request));
  }

  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
    itemService.deleteItem(itemId);
    return ResponseEntity.noContent().build();
  }
}
