package com.kitchen.central.repository;

import com.kitchen.central.entity.Delivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    boolean existsByDeliveryNo(String deliveryNo);

    List<Delivery> findAllByOrderByUpdatedAtDesc();

    List<Delivery> findByBatchIdOrderByCreatedAtAsc(Long batchId);

    /** 这批已经发出去的份数（退回的不算，退回当即把额度还回来） */
    @Query("select coalesce(sum(d.portions), 0) from Delivery d"
            + " where d.batchId = :batchId and d.status <> '已退回'")
    long sumActivePortions(@Param("batchId") Long batchId);

    /** 按批次汇总已发份数（退回的不算），给批次额度查询用 */
    @Query("select d.batchId, sum(d.portions) from Delivery d"
            + " where d.status <> '已退回' group by d.batchId")
    List<Object[]> sumActivePortionsGroupByBatch();
}
