package com.kitchen.central.repository;

import com.kitchen.central.entity.CookingSlot;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingSlotRepository extends JpaRepository<CookingSlot, Long> {

    boolean existsBySlotNo(String slotNo);

    List<CookingSlot> findAllByOrderByUpdatedAtDesc();

    List<CookingSlot> findByKitchenIdAndServeDateAndStatusNotIn(
            Long kitchenId, LocalDate serveDate, Collection<String> statuses);

    List<CookingSlot> findByTeamAndServeDateAndStatusNotIn(
            String team, LocalDate serveDate, Collection<String> statuses);
}
