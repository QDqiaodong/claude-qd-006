package com.kitchen.central.repository;

import com.kitchen.central.entity.Kitchen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenRepository extends JpaRepository<Kitchen, Long> {
    boolean existsByCode(String code);
}
