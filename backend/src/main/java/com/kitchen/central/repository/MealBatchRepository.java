package com.kitchen.central.repository;

import com.kitchen.central.entity.MealBatch;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealBatchRepository extends JpaRepository<MealBatch, Long> {

    boolean existsByBatchNo(String batchNo);

    boolean existsByDishIdAndServeDateAndMealAndStatusNotIn(
            Long dishId, LocalDate serveDate, String meal, Collection<String> statuses);

    List<MealBatch> findAllByOrderByUpdatedAtDesc();

    List<MealBatch> findByServeDateOrderByMealAsc(LocalDate serveDate);

    List<MealBatch> findByKitchenIdAndStatusNotIn(Long kitchenId, Collection<String> statuses);
}
