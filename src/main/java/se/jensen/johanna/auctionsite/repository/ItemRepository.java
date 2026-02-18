package se.jensen.johanna.auctionsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.Item;
import se.jensen.johanna.auctionsite.model.enums.Category;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE (:category IS NULL OR i.category=:category)" +
            " AND (:subCategory IS NULL OR i.subCategory=:subCategory)")
    List<Item> findAllItems(
            @Param("category") Category category,
            @Param("subCategory") Category.SubCategory subCategory
    );
}
