package com.kitchen.central.service;

import com.kitchen.central.dto.BizException;
import com.kitchen.central.entity.Dish;
import com.kitchen.central.entity.Kitchen;
import com.kitchen.central.entity.MealBatch;
import com.kitchen.central.repository.DishRepository;
import com.kitchen.central.repository.KitchenRepository;
import com.kitchen.central.repository.MealBatchRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealService {

    /** 每批留样的最低克数 */
    private static final int MIN_SAMPLE = 125;

    private final MealBatchRepository batches;
    private final DishRepository dishes;
    private final KitchenRepository kitchens;

    public MealService(MealBatchRepository batches, DishRepository dishes, KitchenRepository kitchens) {
        this.batches = batches;
        this.dishes = dishes;
        this.kitchens = kitchens;
    }

    public List<Dish> listDishes(String status, String category, String keyword) {
        return dishes.findAll().stream()
                .filter(d -> status == null || status.isEmpty() || status.equals(d.status))
                .filter(d -> category == null || category.isEmpty() || category.equals(d.category))
                .filter(d -> keyword == null || keyword.isEmpty()
                        || d.name.contains(keyword) || d.code.contains(keyword))
                .toList();
    }

    @Transactional
    public Dish createDish(Dish input) {
        if (input.code == null || input.code.isBlank()) {
            throw new BizException("菜品编号不能为空");
        }
        if (dishes.existsByCode(input.code)) {
            throw new BizException("菜品编号 " + input.code + " 已经存在");
        }
        Dish saved = new Dish();
        saved.code = input.code.trim();
        saved.name = input.name;
        saved.category = (input.category == null || input.category.isBlank()) ? "荤菜" : input.category;
        saved.status = (input.status == null || input.status.isBlank()) ? "在售" : input.status;
        return dishes.save(saved);
    }

    @Transactional
    public Dish updateDish(Long id, Dish input) {
        Dish d = dishes.findById(id).orElseThrow(() -> new BizException("菜品不存在"));
        if (input.name != null) {
            d.name = input.name;
        }
        if (input.category != null && !input.category.isBlank()) {
            d.category = input.category;
        }
        if (input.status != null && !input.status.isBlank()) {
            d.status = input.status;
        }
        return dishes.save(d);
    }

    public List<MealBatch> listBatches(String status, LocalDate date, Long kitchenId, String keyword) {
        List<MealBatch> all = (date == null)
                ? batches.findAllByOrderByUpdatedAtDesc()
                : batches.findByServeDateOrderByMealAsc(date);
        return all.stream()
                .filter(b -> status == null || status.isEmpty() || status.equals(b.status))
                .filter(b -> kitchenId == null || kitchenId.equals(b.kitchenId))
                .filter(b -> keyword == null || keyword.isEmpty()
                        || b.batchNo.contains(keyword)
                        || (b.chef != null && b.chef.contains(keyword)))
                .toList();
    }

    private String nextBatchNo() {
        long n = batches.count() + 1;
        String no;
        do {
            no = "BC-" + String.format("%04d", n++);
        } while (batches.existsByBatchNo(no));
        return no;
    }

    @Transactional
    public MealBatch open(MealBatch input) {
        if (input.dishId == null) {
            throw new BizException("请选一道菜");
        }
        if (input.kitchenId == null) {
            throw new BizException("请选一个操作间");
        }
        if (input.serveDate == null) {
            throw new BizException("请选供应日期");
        }
        if (input.planPortions == null || input.planPortions <= 0) {
            throw new BizException("计划份数要大于 0");
        }
        if (input.chef == null || input.chef.isBlank()) {
            throw new BizException("要写清楚这一批是谁做的");
        }
        if (input.sampleWeight == null || input.sampleWeight < MIN_SAMPLE) {
            throw new BizException("每批都要留样，留样不能少于 " + MIN_SAMPLE + " 克");
        }
        Dish dish = dishes.findById(input.dishId).orElseThrow(() -> new BizException("菜品不存在"));
        if (!"在售".equals(dish.status)) {
            throw new BizException("菜品 " + dish.name + " 已经停用，不能开新批次");
        }
        Kitchen kitchen = kitchens.findById(input.kitchenId)
                .orElseThrow(() -> new BizException("操作间不存在"));
        if (!"在用".equals(kitchen.status)) {
            throw new BizException("操作间 " + kitchen.name + " 现在是" + kitchen.status + "，不能开批次");
        }
        String meal = (input.meal == null || input.meal.isBlank()) ? "午餐" : input.meal;
        if (batches.existsByDishIdAndServeDateAndMealAndStatusNotIn(
                dish.id, input.serveDate, meal, List.of("已作废"))) {
            throw new BizException("菜品 " + dish.name + " 在 " + input.serveDate + " 的" + meal
                    + "已经排过一批了，同一餐不重复排");
        }

        MealBatch saved = new MealBatch();
        saved.batchNo = nextBatchNo();
        saved.dishId = dish.id;
        saved.kitchenId = kitchen.id;
        saved.serveDate = input.serveDate;
        saved.meal = meal;
        saved.planPortions = input.planPortions;
        saved.actualPortions = 0;
        saved.chef = input.chef.trim();
        saved.sampleWeight = input.sampleWeight;
        saved.status = "备料中";
        saved.createdAt = LocalDateTime.now();
        saved.updatedAt = saved.createdAt;
        return batches.save(saved);
    }

    /** 备料中 -> 加工中 -> 已完成；备料中与加工中都能作废。 */
    @Transactional
    public MealBatch advance(Long id, String action, Integer actualPortions) {
        MealBatch b = batches.findById(id).orElseThrow(() -> new BizException("批次不存在"));
        if ("cook".equals(action)) {
            if (!"备料中".equals(b.status)) {
                throw new BizException("只有备料中的批次能开火，这批现在是 " + b.status);
            }
            b.status = "加工中";
        } else if ("done".equals(action)) {
            if (!"加工中".equals(b.status)) {
                throw new BizException("只有加工中的批次能完工，这批现在是 " + b.status);
            }
            if (actualPortions == null || actualPortions <= 0) {
                throw new BizException("完工要登记实际做出来的份数");
            }
            b.actualPortions = actualPortions;
            b.status = "已完成";
        } else if ("scrap".equals(action)) {
            if ("已完成".equals(b.status)) {
                throw new BizException("已经完工的批次不能作废");
            }
            if ("已作废".equals(b.status)) {
                throw new BizException("这批已经作废过了");
            }
            b.status = "已作废";
        } else {
            throw new BizException("不认识的动作：" + action);
        }
        b.updatedAt = LocalDateTime.now();
        return batches.save(b);
    }
}
