package com.restaurant.repository;

import com.restaurant.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategory(String category);

    List<MenuItem> findByAvailableTrue();

    List<MenuItem> findByCategoryAndAvailableTrue(String category);

    List<MenuItem> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT m.category FROM MenuItem m ORDER BY m.category ASC")
    List<String> findAllDistinctCategories();
}
