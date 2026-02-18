package se.jensen.johanna.auctionsite.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.auctionsite.dto.AuctionDTO;
import se.jensen.johanna.auctionsite.dto.AuctionsListDTO;
import se.jensen.johanna.auctionsite.model.enums.Category;
import se.jensen.johanna.auctionsite.service.AuctionService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auctions")
@CrossOrigin
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    /**
     * Retrieves a paginated list of auctions with status ACTIVE for public users to scroll
     * Optional sorting of category and subcategory
     */
    @GetMapping
    public ResponseEntity<Page<AuctionsListDTO>> getAllAuctions(
            @ParameterObject @PageableDefault(size = 20, sort = "endTime", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Category.SubCategory subCategory
    ) {
        return ResponseEntity.ok().body(auctionService.getAllActiveAuctions(category, subCategory, pageable));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionDTO> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getActiveAuction(auctionId));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(Arrays.asList(Category.values()));
    }

    @GetMapping("/subcategories")
    public ResponseEntity<Map<Category, List<Category.SubCategory>>> getSubCategories() {
        Map<Category, List<Category.SubCategory>> subCategories = Arrays.stream(Category.values())
                                                                        .collect(Collectors.toMap(
                                                                                category -> category,
                                                                                Category.SubCategory::getAllSubsByCategory
                                                                        ));
        return ResponseEntity.ok(subCategories);
    }
}
