package se.jensen.johanna.auctionsite.controller.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.jensen.johanna.auctionsite.dto.admin.AdminItemResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateItemRequest;
import se.jensen.johanna.auctionsite.dto.admin.UpdateItemRequest;
import se.jensen.johanna.auctionsite.service.ItemService;

@Controller
@RequestMapping("/web/admin/items")
@RequiredArgsConstructor
public class AdminItemWebController {

  private final ItemService itemService;

  @PostMapping("/create")
  public String createItem(@Valid @ModelAttribute CreateItemRequest request,
      BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      bindingResult.getAllErrors().forEach(System.out::println);
      // Om det finns valideringsfel, visa formuläret igen istället för att gå vidare
      return "admin-create-item";
    }
    AdminItemResponse response = itemService.createItem(request);
    model.addAttribute("itemId", response.itemId());
    return "redirect:/web/admin/items/" + response.itemId();
  }

  @GetMapping("/create")
  public String showCreateItemForm(Model model) {
    model.addAttribute("createItemRequest",
        new CreateItemRequest(null, null, null, null, null, null,
            null));
    return "admin-create-item";
  }

  @GetMapping("/{itemId}")
  public String showItemDetails(@PathVariable Long itemId, Model model) {
    AdminItemResponse response = itemService.findItem(itemId);
    model.addAttribute("item", response);
    return "admin-item-details";
  }

  @GetMapping("/{itemId}/edit")
  public String showEditForm(@PathVariable Long itemId, Model model) {
    AdminItemResponse item = itemService.findItem(itemId);
    model.addAttribute("updateItemRequest",
        new UpdateItemRequest(item.category(), item.subCategory(), item.title(), item.description(),
            item.valuation(), item.imageUrls(), null));
    model.addAttribute("itemId", itemId);
    return "admin-edit-item";
  }

  @PostMapping("/{itemId}/update")
  public String updateItem(@PathVariable Long itemId,
      @Valid @ModelAttribute("updateItemRequest") UpdateItemRequest request,
      BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("itemId", itemId);
      return "admin-edit-item";
    }
    itemService.updateItem(itemId, request);
    return "redirect:/web/admin/items/" + itemId;
  }

}
