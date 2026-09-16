package com.kitchen.central.repository;

import com.kitchen.central.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByName(String name);

    /**
     * 原子扣库存：库存够才扣得动，并发扣同一袋也不会扣成负数，
     * 只返回受影响行数，0 就是库存不够。
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Ingredient i SET i.stock = i.stock - :qty WHERE i.id = :id AND i.stock >= :qty")
    int deductStock(@Param("id") Long id, @Param("qty") int qty);
}
