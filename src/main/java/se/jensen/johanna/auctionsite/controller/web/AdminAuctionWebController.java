package se.jensen.johanna.auctionsite.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateAuctionRequest;
import se.jensen.johanna.auctionsite.service.AuctionService;

@Controller
@RequestMapping("/web/admin")
@RequiredArgsConstructor
public class AdminAuctionWebController {

  private final AuctionService auctionService;

  @PostMapping("/items/{itemId}/auctions")
  public String createAuctionForItem(@PathVariable Long itemId,
      @ModelAttribute CreateAuctionRequest request, Model model) {
    AdminAuctionResponse response = auctionService.createAuctionForItem(itemId, request);
    return "redirect:/web/admin/auctions/" + response.auctionId();

  }

  @GetMapping("items/{itemId}/auctions")
  public String showCreateAuctionForm(@PathVariable Long itemId, Model model) {
    model.addAttribute("itemId", itemId);
    model.addAttribute("createAuctionRequest", new CreateAuctionRequest(null));
    return "admin-prepare-auction";

  }

  @GetMapping("/auctions/{auctionId}")
  public String showAuctionDetails(@PathVariable Long auctionId, Model model) {
    AdminAuctionResponse response = auctionService.getAuction(auctionId);
    model.addAttribute("auction", response);
    return "admin-auction-details";
  }

}
