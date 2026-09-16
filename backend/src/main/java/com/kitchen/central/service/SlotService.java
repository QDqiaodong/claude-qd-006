package com.kitchen.central.service;

import com.kitchen.central.dto.BizException;
import com.kitchen.central.entity.CookingSlot;
import com.kitchen.central.entity.Equipment;
import com.kitchen.central.entity.Kitchen;
import com.kitchen.central.repository.CookingSlotRepository;
import com.kitchen.central.repository.EquipmentRepository;
import com.kitchen.central.repository.KitchenRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {

    private final CookingSlotRepository slots;
    private final KitchenRepository kitchens;
    private final EquipmentRepository equipments;

    public SlotService(CookingSlotRepository slots, KitchenRepository kitchens,
                       EquipmentRepository equipments) {
        this.slots = slots;
        this.kitchens = kitchens;
        this.equipments = equipments;
    }

    public List<CookingSlot> list(LocalDate date, String status, Long kitchenId, String team) {
        return slots.findAllByOrderByUpdatedAtDesc().stream()
                .filter(s -> date == null || date.equals(s.serveDate))
                .filter(s -> status == null || status.isEmpty() || status.equals(s.status))
                .filter(s -> kitchenId == null || kitchenId.equals(s.kitchenId))
                .filter(s -> team == null || team.isEmpty() || team.equals(s.team))
                .toList();
    }

    private String nextSlotNo() {
        long n = slots.count() + 1;
        String no;
        do {
            no = "SL-" + String.format("%04d", n++);
        } while (slots.existsBySlotNo(no));
        return no;
    }

    @Transactional
    public CookingSlot open(CookingSlot input) {
        if (input.kitchenId == null) {
            throw new BizException("请选一个操作间");
        }
        if (input.serveDate == null) {
            throw new BizException("请选日期");
        }
        if (input.team == null || input.team.isBlank()) {
            throw new BizException("要写清楚是哪个班组");
        }
        if (input.startMin == null || input.endMin == null || input.endMin <= input.startMin) {
            throw new BizException("结束时间必须晚于开始时间");
        }
        Kitchen kitchen = kitchens.findById(input.kitchenId)
                .orElseThrow(() -> new BizException("操作间不存在"));
        if (!"在用".equals(kitchen.status)) {
            throw new BizException("操作间 " + kitchen.name + " 现在是" + kitchen.status + "，不能排期");
        }
        List<Equipment> bad = equipments.findAll().stream()
                .filter(e -> kitchen.id.equals(e.kitchenId))
                .filter(e -> !"可用".equals(e.status))
                .toList();
        if (!bad.isEmpty()) {
            throw new BizException("操作间 " + kitchen.name + " 上的 " + bad.get(0).name
                    + " 现在是" + bad.get(0).status + "，先处理这台设备再排期");
        }
        String team = input.team.trim();
        for (CookingSlot other : slots.findByKitchenIdAndServeDateAndStatusNotIn(
                kitchen.id, input.serveDate, List.of("已取消"))) {
            if (overlap(other, input)) {
                throw new BizException("操作间 " + kitchen.name + " 这个时段已经被排期 "
                        + other.slotNo + " 占了");
            }
        }
        for (CookingSlot other : slots.findByTeamAndServeDateAndStatusNotIn(
                team, input.serveDate, List.of("已取消"))) {
            if (overlap(other, input)) {
                throw new BizException("班组 " + team + " 这个时段已经排了 "
                        + other.slotNo + "，一个班组不能同时占两个操作间");
            }
        }

        CookingSlot saved = new CookingSlot();
        saved.slotNo = nextSlotNo();
        saved.kitchenId = kitchen.id;
        saved.serveDate = input.serveDate;
        saved.meal = (input.meal == null || input.meal.isBlank()) ? "午餐" : input.meal;
        saved.team = team;
        saved.startMin = input.startMin;
        saved.endMin = input.endMin;
        saved.status = "待开始";
        saved.createdAt = LocalDateTime.now();
        saved.updatedAt = saved.createdAt;
        return slots.save(saved);
    }

    private boolean overlap(CookingSlot other, CookingSlot input) {
        if (other.startMin == null || other.endMin == null) {
            return false;
        }
        return other.startMin < input.endMin && input.startMin < other.endMin;
    }

    @Transactional
    public CookingSlot advance(Long id, String action) {
        CookingSlot s = slots.findById(id).orElseThrow(() -> new BizException("排期不存在"));
        if ("start".equals(action)) {
            if (!"待开始".equals(s.status)) {
                throw new BizException("只有待开始的排期能开工，这条现在是 " + s.status);
            }
            s.status = "进行中";
        } else if ("done".equals(action)) {
            if (!"进行中".equals(s.status)) {
                throw new BizException("只有进行中的排期能收工，这条现在是 " + s.status);
            }
            s.status = "已完成";
        } else if ("cancel".equals(action)) {
            if ("已完成".equals(s.status)) {
                throw new BizException("已经收工的排期不能取消");
            }
            if ("已取消".equals(s.status)) {
                throw new BizException("这条排期已经取消过了");
            }
            s.status = "已取消";
        } else {
            throw new BizException("不认识的动作：" + action);
        }
        s.updatedAt = LocalDateTime.now();
        return slots.save(s);
    }
}
