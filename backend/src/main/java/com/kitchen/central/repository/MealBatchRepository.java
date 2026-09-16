package com.kitchen.central.repository;

import com.kitchen.central.entity.MealBatch;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealBatchRepository extends JpaRepository<MealBatch, Long> {

    boolean existsByBatchNo(String batchNo);

    /** 开配送单时先锁住这一批（SELECT ... FOR UPDATE）：并发的开单排队等锁，核完份数才放行 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from MealBatch b where b.id = :id")
    Optional<MealBatch> findByIdForUpdate(@Param("id") Long id);

    boolean existsByDishIdAndServeDateAndMealAndStatusNotIn(
            Long dishId, LocalDate serveDate, String meal, Collection<String> statuses);

    List<MealBatch> findAllByOrderByUpdatedAtDesc();

    List<MealBatch> findByServeDateOrderByMealAsc(LocalDate serveDate);

    List<MealBatch> findByKitchenIdAndStatusNotIn(Long kitchenId, Collection<String> statuses);
}
