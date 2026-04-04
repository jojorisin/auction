package se.jensen.johanna.auctionsite.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import se.jensen.johanna.auctionsite.service.AuctionService;

@Controller
@RequestMapping("/web/auctions")
@RequiredArgsConstructor
public class AuctionWebController {

  private final AuctionService auctionService;

  /*@GetMapping
  public String showAuctionsList(
      @ParameterObject @PageableDefault(size = 20, sort = "endTime", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Category.SubCategory subCategory, Model model) {
    Page<AuctionsListResponse> auctions = auctionService.getAllActiveAuctions(category, subCategory,
        pageable);
    System.out.println(auctions.getSize());
    model.addAttribute("categories", Category.values());
    model.addAttribute("auctions", auctions);
    model.addAttribute("selectedCategory", category);
    model.addAttribute("selectedSubCategory", subCategory);
    return "auctions-list";

  }*/

}
