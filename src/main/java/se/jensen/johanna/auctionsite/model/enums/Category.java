package se.jensen.johanna.auctionsite.model.enums;

import java.util.ArrayList;
import java.util.List;

public enum Category {
    ART, FURNITURE, CERAMICS_AND_PORCELAIN, CLOCKS_AND_WATCHES, JEWELLERY,
    LIGHTING, GLASS, CARPETS_AND_TEXTILES, COLLECTABLES, SILVER_AND_METALS, FASHION, TOYS,
    MILITARIA, BOOKS, VEHICLES, ELECTRONICS, MIRRORS, ALCOHOL, MISCELLANEOUS;

    public enum SubCategory {
        PHOTOGRAPHY(Category.ART), SCULPTURES(Category.ART), PAINTINGS(Category.ART), PRINTS(Category.ART), OTHER_ART(Category.ART),
        CHAIRS(Category.FURNITURE), COFFEE_TABLES(Category.FURNITURE), CABINETS_AND_SHELVES(Category.FURNITURE),
        DESKS(Category.FURNITURE), DINING_TABLES(Category.FURNITURE), SOFAS_AND_ARMCHAIRS(Category.FURNITURE),
        TABLEWARE(Category.CERAMICS_AND_PORCELAIN),
        WRIST_WATCHES(Category.CLOCKS_AND_WATCHES), WALL_CLOCKS(Category.CLOCKS_AND_WATCHES), POCKET_WATCHES(Category.CLOCKS_AND_WATCHES),
        MANTEL_CLOCKS(Category.CLOCKS_AND_WATCHES), LONGCASE_CLOCKS(Category.CLOCKS_AND_WATCHES),
        BRACELETS(Category.JEWELLERY), EARRINGS(Category.JEWELLERY), NECKLACES(Category.JEWELLERY), BROOCHES(Category.JEWELLERY),
        CUFFLINKS(Category.JEWELLERY), GEMSTONES(Category.JEWELLERY), RINGS(Category.JEWELLERY), TIARA(Category.JEWELLERY), OTHER_JEWELLERY(Category.JEWELLERY),
        CEILING_LIGHTS(Category.LIGHTING), CHANDELIERS(Category.LIGHTING), FLOOR_LIGHTS(Category.LIGHTING), TABLE_LAMPS(Category.LIGHTING),
        WALL_LIGHTS(Category.LIGHTING), CANDLESTICKS(Category.LIGHTING), OTHER_LIGHTING(Category.LIGHTING),
        ART_GLASS(Category.GLASS), TABLEWARE_GLASS(Category.GLASS), OTHER_GLASS(Category.GLASS),
        CARPETS(Category.CARPETS_AND_TEXTILES), TEXTILES(Category.CARPETS_AND_TEXTILES), ORIENTAL_CARPETS(Category.CARPETS_AND_TEXTILES),
        PERSIAN_CARPETS(Category.CARPETS_AND_TEXTILES), EUROPEAN_CARPETS(Category.CARPETS_AND_TEXTILES), OTHER_TEXTILES(Category.CARPETS_AND_TEXTILES),
        ADS_AND_SIGNS(Category.COLLECTABLES), AUDIO_VINYL_HIFI(Category.COLLECTABLES), TRADING_CARDS(Category.COLLECTABLES),
        MOVIE_MEMORABILIA(Category.COLLECTABLES), MUSIC_MEMORABILIA(Category.COLLECTABLES), MUSICAL_INSTRUMENTS(Category.COLLECTABLES),
        PENS(Category.COLLECTABLES), SPORTS_MEMORABILIA(Category.COLLECTABLES), TECHNICA_AND_NAUTICA(Category.COLLECTABLES),
        OTHER_COLLACTABLES(Category.COLLECTABLES),
        PEWTER_BRASS_COPPER(Category.SILVER_AND_METALS), SILVER(Category.SILVER_AND_METALS), OTHER_METALS(Category.SILVER_AND_METALS),
        ACCESSORIES(Category.FASHION), CLOTHING(Category.FASHION),
        ACTION_FIGURES_AND_SCI_FI(Category.TOYS), DOLLS(Category.TOYS), PLUSH_TOYS(Category.TOYS), MODEL_TOYS(Category.TOYS),
        OTHER_TOYS(Category.TOYS),
        AUTOGRAPHS_MANUSCRIPTS(Category.BOOKS), BOOKS(Category.BOOKS), MAPS(Category.BOOKS), OTHER_BOOKS(Category.BOOKS),
        ;
        //fortsätt med alla...


        private final Category category;

        SubCategory(Category category) {
            this.category = category;

        }

        public static List<SubCategory> getAllSubsByCategory(Category category) {
            List<SubCategory> subCategories = new ArrayList<>();
            for (SubCategory subCategory : values()) {
                if (subCategory.category.equals(category)) {
                    subCategories.add(subCategory);
                }

            }
            return subCategories;
        }

        public Category getCategory() {
            return category;
        }
    }
}
