package com.kitchen.central.service;

import com.kitchen.central.dto.BizException;
import com.kitchen.central.entity.Equipment;
import com.kitchen.central.entity.Kitchen;
import com.kitchen.central.repository.CookingSlotRepository;
import com.kitchen.central.repository.EquipmentRepository;
import com.kitchen.central.repository.KitchenRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitchenService {

    private final KitchenRepository kitchens;
    private final EquipmentRepository equipments;
    private final CookingSlotRepository slots;

    public KitchenService(KitchenRepository kitchens, EquipmentRepository equipments,
                          CookingSlotRepository slots) {
        this.kitchens = kitchens;
        this.equipments = equipments;
        this.slots = slots;
    }

    public List<Kitchen> listKitchens(String status, String keyword) {
        return kitchens.findAll().stream()
                .filter(k -> status == null || status.isEmpty() || status.equals(k.status))
                .filter(k -> keyword == null || keyword.isEmpty()
                        || k.name.contains(keyword) || k.code.contains(keyword))
                .toList();
    }

    @Transactional
    public Kitchen createKitchen(Kitchen input) {
        if (input.code == null || input.code.isBlank()) {
            throw new BizException("操作间编号不能为空");
        }
        if (kitchens.existsByCode(input.code)) {
            throw new BizException("操作间编号 " + input.code + " 已经存在");
        }
        Kitchen saved = new Kitchen();
        saved.code = input.code.trim();
        saved.name = input.name;
        saved.kind = (input.kind == null || input.kind.isBlank()) ? "热厨" : input.kind;
        saved.status = (input.status == null || input.status.isBlank()) ? "在用" : input.status;
        return kitchens.save(saved);
    }

    @Transactional
    public Kitchen updateKitchen(Long id, Kitchen input) {
        Kitchen k = kitchens.findById(id).orElseThrow(() -> new BizException("操作间不存在"));
        if (input.name != null) {
            k.name = input.name;
        }
        if (input.kind != null && !input.kind.isBlank()) {
            k.kind = input.kind;
        }
        if (input.status != null && !input.status.isBlank() && !input.status.equals(k.status)) {
            if (!"在用".equals(input.status)
                    && !slots.findByKitchenIdAndServeDateAndStatusNotIn(
                            k.id, LocalDate.now(), List.of("已取消", "已完成")).isEmpty()) {
                throw new BizException("操作间 " + k.name + " 今天还有没结束的排期，先处理完再改成"
                        + input.status);
            }
            k.status = input.status;
        }
        return kitchens.save(k);
    }

    public List<Equipment> listEquipments(Long kitchenId, String status, String keyword) {
        return equipments.findAll().stream()
                .filter(e -> kitchenId == null || kitchenId.equals(e.kitchenId))
                .filter(e -> status == null || status.isEmpty() || status.equals(e.status))
                .filter(e -> keyword == null || keyword.isEmpty()
                        || e.name.contains(keyword) || e.code.contains(keyword))
                .toList();
    }

    @Transactional
    public Equipment createEquipment(Equipment input) {
        if (input.code == null || input.code.isBlank()) {
            throw new BizException("设备编号不能为空");
        }
        if (equipments.existsByCode(input.code)) {
            throw new BizException("编号 " + input.code + " 已经被别的设备用掉了");
        }
        if (input.kitchenId != null && !kitchens.existsById(input.kitchenId)) {
            throw new BizException("归属的操作间不存在");
        }
        Equipment saved = new Equipment();
        saved.code = input.code.trim();
        saved.name = input.name;
        saved.category = (input.category == null || input.category.isBlank()) ? "灶具" : input.category;
        saved.kitchenId = input.kitchenId;
        saved.status = (input.status == null || input.status.isBlank()) ? "可用" : input.status;
        return equipments.save(saved);
    }

    @Transactional
    public Equipment updateEquipment(Long id, Equipment input) {
        Equipment e = equipments.findById(id).orElseThrow(() -> new BizException("设备不存在"));
        if (input.name != null) {
            e.name = input.name;
        }
        if (input.category != null && !input.category.isBlank()) {
            e.category = input.category;
        }
        if (input.kitchenId != null && !input.kitchenId.equals(e.kitchenId)) {
            if (!kitchens.existsById(input.kitchenId)) {
                throw new BizException("要挪过去的操作间不存在");
            }
            e.kitchenId = input.kitchenId;
        }
        if (input.status != null && !input.status.isBlank() && !input.status.equals(e.status)) {
            if (!"可用".equals(input.status) && e.kitchenId != null
                    && !slots.findByKitchenIdAndServeDateAndStatusNotIn(
                            e.kitchenId, LocalDate.now(), List.of("已取消", "已完成")).isEmpty()) {
                throw new BizException("这台设备所在操作间今天还有没结束的排期，先处理完再改成"
                        + input.status);
            }
            e.status = input.status;
        }
        return equipments.save(e);
    }
}
