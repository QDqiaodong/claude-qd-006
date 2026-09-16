package com.kitchen.central.repository;

import com.kitchen.central.entity.Delivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    boolean existsByDeliveryNo(String deliveryNo);

    List<Delivery> findAllByOrderByUpdatedAtDesc();

    List<Delivery> findByBatchIdOrderByCreatedAtAsc(Long batchId);
}
