package com.kitchen.central.service;

import com.kitchen.central.dto.BatchQuota;
import com.kitchen.central.dto.BizException;
import com.kitchen.central.entity.Delivery;
import com.kitchen.central.entity.MealBatch;
import com.kitchen.central.repository.DeliveryRepository;
import com.kitchen.central.repository.MealBatchRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final MealBatchRepository batches;

    public DeliveryService(DeliveryRepository deliveries, MealBatchRepository batches) {
        this.deliveries = deliveries;
        this.batches = batches;
    }

    public List<Delivery> list(String status, Long batchId) {
        List<Delivery> all = (batchId == null)
                ? deliveries.findAllByOrderByUpdatedAtDesc()
                : deliveries.findByBatchIdOrderByCreatedAtAsc(batchId);
        return all.stream()
                .filter(d -> status == null || status.isEmpty() || status.equals(d.status))
                .toList();
    }

    private String nextDeliveryNo() {
        long n = deliveries.count() + 1;
        String no;
        do {
            no = "PS-" + String.format("%04d", n++);
        } while (deliveries.existsByDeliveryNo(no));
        return no;
    }

    private int sentPortions(Long batchId) {
        return Math.toIntExact(deliveries.sumActivePortions(batchId));
    }

    /** 各批次的出餐额度：已发（不含退回）与还能发多少，前台展示一律以这里算的为准。 */
    public List<BatchQuota> quotas() {
        Map<Long, Long> sentByBatch = new HashMap<>();
        for (Object[] row : deliveries.sumActivePortionsGroupByBatch()) {
            sentByBatch.put((Long) row[0], ((Number) row[1]).longValue());
        }
        List<BatchQuota> out = new ArrayList<>();
        for (MealBatch b : batches.findAllById(sentByBatch.keySet())) {
            out.add(new BatchQuota(b.id, b.actualPortions,
                    Math.toIntExact(sentByBatch.get(b.id))));
        }
        return out;
    }

    // 对账员规矩：落账当时份数不许越过实际产量。先锁住批次行再核对，
    // 同时来的开单排队等锁，轮到它时重新按最新已发份数核，超发的那张拦下不留；
    // READ_COMMITTED 保证等锁结束后立刻能看到先到的单子刚落账的份数
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Delivery open(Delivery input) {
        if (input.batchId == null) {
            throw new BizException("请选一个备餐批次");
        }
        if (input.customer == null || input.customer.isBlank()) {
            throw new BizException("收货单位不能为空");
        }
        if (input.portions == null || input.portions <= 0) {
            throw new BizException("配送份数要大于 0");
        }
        MealBatch batch = batches.findByIdForUpdate(input.batchId)
                .orElseThrow(() -> new BizException("批次不存在"));
        if (!"已完成".equals(batch.status)) {
            throw new BizException("批次 " + batch.batchNo + " 现在是 " + batch.status + "，还不能出餐");
        }
        int sent = sentPortions(batch.id);
        if (sent + input.portions > batch.actualPortions) {
            throw new BizException("批次 " + batch.batchNo + " 实际做出 " + batch.actualPortions
                    + " 份，已经发掉 " + sent + " 份，这单要发 " + input.portions + " 份不够");
        }

        Delivery saved = new Delivery();
        saved.deliveryNo = nextDeliveryNo();
        saved.batchId = batch.id;
        saved.customer = input.customer.trim();
        saved.portions = input.portions;
        saved.driver = input.driver;
        saved.status = "待发";
        saved.createdAt = LocalDateTime.now();
        saved.updatedAt = saved.createdAt;
        return deliveries.save(saved);
    }

    @Transactional
    public Delivery advance(Long id, String action, String driver, LocalDate deliverDate) {
        Delivery d = deliveries.findById(id).orElseThrow(() -> new BizException("配送单不存在"));
        if ("send".equals(action)) {
            if (!"待发".equals(d.status)) {
                throw new BizException("只有待发的单子能发车，这单现在是 " + d.status);
            }
            if (driver != null && !driver.isBlank()) {
                d.driver = driver;
            }
            if (d.driver == null || d.driver.isBlank()) {
                throw new BizException("发车要写司机");
            }
            d.deliverDate = deliverDate == null ? LocalDate.now() : deliverDate;
            d.status = "在途";
        } else if ("sign".equals(action)) {
            if (!"在途".equals(d.status)) {
                throw new BizException("只有在途的单子能签收，这单现在是 " + d.status);
            }
            d.status = "已签收";
        } else if ("back".equals(action)) {
            if (!"在途".equals(d.status)) {
                throw new BizException("只有还没签收的在途单能退回，这单现在是 " + d.status);
            }
            d.status = "已退回";
        } else {
            throw new BizException("不认识的动作：" + action);
        }
        d.updatedAt = LocalDateTime.now();
        return deliveries.save(d);
    }
}
