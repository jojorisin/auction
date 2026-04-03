package se.jensen.johanna.auctionsite.controller.web;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.jensen.johanna.auctionsite.dto.admin.AdminAuctionResponse;
import se.jensen.johanna.auctionsite.dto.admin.CreateAuctionRequest;
import se.jensen.johanna.auctionsite.dto.admin.LaunchRequest;
import se.jensen.johanna.auctionsite.dto.admin.LaunchResponse;
import se.jensen.johanna.auctionsite.model.enums.AuctionStatus;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.AuctionService;

@Controller
@RequestMapping("/web/admin")
@RequiredArgsConstructor
public class AdminAuctionWebController {

  private final AuctionService auctionService;

  @GetMapping("/auctions")
  public String showAuctionList(
      @ParameterObject @PageableDefault(size = 20, sort = "endTime", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Category.SubCategory subCategory,
      @RequestParam(required = false) AuctionStatus status, Model model) {
    Page<AdminAuctionResponse> auctions = auctionService.getAllAuctions(category, subCategory,
        status,
        pageable);
    model.addAttribute("auctions", auctions);
    return "admin-auctions-list";
  }

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

  @PostMapping("/auctions/launch")
  public String launch(@ModelAttribute LaunchRequest request, Model model) {
    LaunchResponse response = auctionService.launchBatch(request);
    return "redirect:/web/admin/auctions";
  }

  @GetMapping("/auctions/launch")
  public String showLaunchForm(Model model) {
    ZonedDateTime nowStockholm = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"));

    LocalDate startDate = nowStockholm.toLocalDate();
    LocalTime startTime = nowStockholm.toLocalTime().withSecond(0).withNano(0);

    LocalDate endDate = startDate.plusDays(7);
    LocalTime endTime = startTime;

    model.addAttribute("launchRequest", new LaunchRequest(
        50,
        AuctionStatus.INACTIVE,
        startDate,
        startTime,
        endDate,
        endTime
    ));
    return "admin-launch";

  }

}
