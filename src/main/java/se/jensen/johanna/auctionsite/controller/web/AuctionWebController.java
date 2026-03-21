package se.jensen.johanna.auctionsite.controller.web;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.jensen.johanna.auctionsite.dto.AuctionsListResponse;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.AuctionService;

@Controller
@RequestMapping("/web/auctions")
@RequiredArgsConstructor
public class AuctionWebController {

  private final AuctionService auctionService;

  @GetMapping
  public String showAuctionsList(
      @ParameterObject @PageableDefault(size = 20, sort = "endTime", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Category.SubCategory subCategory, Model model) {
    Page<AuctionsListResponse> auctions = auctionService.getAllActiveAuctions(category, subCategory,
        pageable);
    System.out.println(auctions.getSize());

    model.addAttribute("auctions", auctions);
    model.addAttribute("selectedCategory", category);
    model.addAttribute("selectedSubCategory", subCategory);
    return "auctions-list";

  }

}
