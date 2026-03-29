package se.jensen.johanna.auctionsite.model.enums;

import java.util.ArrayList;
import java.util.List;

public enum Category {
  ART, FURNITURE, TABLEWARE, CLOCKS_AND_WATCHES, JEWELLERY, LIGHTING, CARPETS_AND_TEXTILES, COLLECTABLES, FASHION, TOYS, BOOKS, ELECTRONICS, MIRRORS, MISCELLANEOUS;

  public static List<SubCategory> getAllSubsByCategory(Category category) {
    List<SubCategory> subCategories = new ArrayList<>();
    for (SubCategory subCategory : SubCategory.values()) {
      if (subCategory.getCategory().equals(category)) {
        subCategories.add(subCategory);
      }

    }
    return subCategories;
  }

  public enum SubCategory {
    PHOTOGRAPHY(Category.ART), SCULPTURES(Category.ART), PAINTINGS(Category.ART), PRINTS(
        Category.ART), OTHER_ART(Category.ART), CHAIRS(Category.FURNITURE), COFFEE_TABLES(
        Category.FURNITURE), CABINETS_AND_SHELVES(Category.FURNITURE), DESKS(
        Category.FURNITURE), DINING_TABLES(Category.FURNITURE), SOFAS_AND_ARMCHAIRS(
        Category.FURNITURE), TABLEWARE(Category.TABLEWARE), TABLEWARE_GLASS(
        Category.TABLEWARE), OTHER_GLASS(Category.TABLEWARE), WRIST_WATCHES(
        Category.CLOCKS_AND_WATCHES), WALL_CLOCKS(Category.CLOCKS_AND_WATCHES), POCKET_WATCHES(
        Category.CLOCKS_AND_WATCHES), MANTEL_CLOCKS(Category.CLOCKS_AND_WATCHES), LONGCASE_CLOCKS(
        Category.CLOCKS_AND_WATCHES), BRACELETS(Category.JEWELLERY), EARRINGS(
        Category.JEWELLERY), NECKLACES(Category.JEWELLERY), BROOCHES(Category.JEWELLERY), CUFFLINKS(
        Category.JEWELLERY), GEMSTONES(Category.JEWELLERY), RINGS(Category.JEWELLERY), TIARA(
        Category.JEWELLERY), OTHER_JEWELLERY(Category.JEWELLERY), CEILING_LIGHTS(
        Category.LIGHTING), CHANDELIERS(Category.LIGHTING), FLOOR_LIGHTS(
        Category.LIGHTING), TABLE_LAMPS(Category.LIGHTING), WALL_LIGHTS(
        Category.LIGHTING), CANDLESTICKS(Category.LIGHTING), OTHER_LIGHTING(
        Category.LIGHTING), CARPETS(Category.CARPETS_AND_TEXTILES), TEXTILES(
        Category.CARPETS_AND_TEXTILES), ORIENTAL_CARPETS(
        Category.CARPETS_AND_TEXTILES), PERSIAN_CARPETS(
        Category.CARPETS_AND_TEXTILES), EUROPEAN_CARPETS(
        Category.CARPETS_AND_TEXTILES), OTHER_TEXTILES(
        Category.CARPETS_AND_TEXTILES), ADS_AND_SIGNS(Category.COLLECTABLES), AUDIO_VINYL_HIFI(
        Category.COLLECTABLES), TRADING_CARDS(Category.COLLECTABLES), MOVIE_MEMORABILIA(
        Category.COLLECTABLES), MUSIC_MEMORABILIA(Category.COLLECTABLES), MUSICAL_INSTRUMENTS(
        Category.COLLECTABLES), PENS(Category.COLLECTABLES), SPORTS_MEMORABILIA(
        Category.COLLECTABLES), OTHER_COLLACTABLES(
        Category.COLLECTABLES), ACCESSORIES(
        Category.FASHION), CLOTHING(Category.FASHION), ACTION_FIGURES_AND_SCI_FI(
        Category.TOYS), DOLLS(Category.TOYS), PLUSH_TOYS(Category.TOYS), MODEL_TOYS(
        Category.TOYS), OTHER_TOYS(Category.TOYS), AUTOGRAPHS_MANUSCRIPTS(Category.BOOKS), BOOKS(
        Category.BOOKS), MAPS(Category.BOOKS), OTHER_BOOKS(Category.BOOKS),
    ;
    

    private final Category category;

    SubCategory(Category category) {
      this.category = category;

    }


    public Category getCategory() {
      return category;
    }
  }
}
