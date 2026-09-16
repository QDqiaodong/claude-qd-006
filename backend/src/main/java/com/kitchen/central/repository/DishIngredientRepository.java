package com.kitchen.central.repository;

import com.kitchen.central.entity.DishIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishIngredientRepository extends JpaRepository<DishIngredient, Long> {

    List<DishIngredient> findByDishId(Long dishId);

    void deleteByDishId(Long dishId);
}
