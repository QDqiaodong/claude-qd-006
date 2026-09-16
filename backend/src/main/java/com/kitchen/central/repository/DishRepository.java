package com.kitchen.central.repository;

import com.kitchen.central.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
    boolean existsByCode(String code);
}
