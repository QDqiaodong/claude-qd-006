package com.kitchen.central.repository;

import com.kitchen.central.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsByCode(String code);
}
