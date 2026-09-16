package com.kitchen.central.service;

import com.kitchen.central.dto.BizException;
import com.kitchen.central.entity.DishIngredient;
import com.kitchen.central.entity.Ingredient;
import com.kitchen.central.repository.DishIngredientRepository;
import com.kitchen.central.repository.DishRepository;
import com.kitchen.central.repository.IngredientRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientService {

    private final IngredientRepository ingredients;
    private final DishIngredientRepository dishIngredients;
    private final DishRepository dishes;

    public IngredientService(IngredientRepository ingredients,
                             DishIngredientRepository dishIngredients,
                             DishRepository dishes) {
        this.ingredients = ingredients;
        this.dishIngredients = dishIngredients;
        this.dishes = dishes;
    }

    public List<Ingredient> list(String keyword) {
        return ingredients.findAll().stream()
                .filter(i -> keyword == null || keyword.isEmpty() || i.name.contains(keyword))
                .toList();
    }

    @Transactional
    public Ingredient create(Ingredient input) {
        if (input.name == null || input.name.isBlank()) {
            throw new BizException("原料名称不能为空");
        }
        if (ingredients.existsByName(input.name.trim())) {
            throw new BizException("原料 " + input.name + " 已经在台账里了");
        }
        if (input.stock == null || input.stock < 0) {
            throw new BizException("库存不能是负数");
        }
        if (input.expiryDate == null) {
            throw new BizException("要登记保质期");
        }
        Ingredient saved = new Ingredient();
        saved.name = input.name.trim();
        saved.stock = input.stock;
        saved.expiryDate = input.expiryDate;
        return ingredients.save(saved);
    }

    @Transactional
    public Ingredient update(Long id, Ingredient input) {
        Ingredient ing = ingredients.findById(id).orElseThrow(() -> new BizException("原料不存在"));
        if (input.name != null && !input.name.isBlank()) {
            ing.name = input.name.trim();
        }
        if (input.stock != null) {
            if (input.stock < 0) {
                throw new BizException("库存不能是负数");
            }
            ing.stock = input.stock;
        }
        if (input.expiryDate != null) {
            ing.expiryDate = input.expiryDate;
        }
        return ingredients.save(ing);
    }

    public List<DishIngredient> recipeOf(Long dishId) {
        return dishIngredients.findByDishId(dishId);
    }

    /** 整组替换一道菜的配方。 */
    @Transactional
    public List<DishIngredient> saveRecipe(Long dishId, List<DishIngredient> lines) {
        dishes.findById(dishId).orElseThrow(() -> new BizException("菜品不存在"));
        dishIngredients.deleteByDishId(dishId);
        if (lines == null) {
            return List.of();
        }
        List<DishIngredient> saved = new ArrayList<>();
        for (DishIngredient line : lines) {
            if (line.ingredientId == null) {
                throw new BizException("配方里有一行没选原料");
            }
            if (line.quantity == null || line.quantity <= 0) {
                throw new BizException("每份用量要大于 0");
            }
            ingredients.findById(line.ingredientId)
                    .orElseThrow(() -> new BizException("原料 #" + line.ingredientId + " 不存在"));
            DishIngredient row = new DishIngredient();
            row.dishId = dishId;
            row.ingredientId = line.ingredientId;
            row.quantity = line.quantity;
            saved.add(dishIngredients.save(row));
        }
        return saved;
    }

    /**
     * 按这道菜的配方扣掉 portions 份的原料。
     * 过期原料不能耗；库存不够就整笔拒绝（抛异常，调用方事务一起回滚）。
     * 扣减走原子 UPDATE，并发扣同一袋不会扣成负数，先到的那笔成功。
     */
    @Transactional
    public void consumeForDish(Long dishId, int portions) {
        List<DishIngredient> recipe = dishIngredients.findByDishId(dishId);
        LocalDate today = LocalDate.now();
        for (DishIngredient line : recipe) {
            if (line.quantity == null || line.quantity <= 0) {
                continue;
            }
            Ingredient ing = ingredients.findById(line.ingredientId)
                    .orElseThrow(() -> new BizException("配方里的原料 #" + line.ingredientId + " 不存在"));
            if (ing.expiryDate != null && ing.expiryDate.isBefore(today)) {
                throw new BizException("原料「" + ing.name + "」保质期到 " + ing.expiryDate
                        + "，已经过期，不能耗用");
            }
            int need = line.quantity * portions;
            int rows = ingredients.deductStock(ing.id, need);
            if (rows == 0) {
                int left = ingredients.findById(ing.id).map(i -> i.stock).orElse(0);
                throw new BizException("原料「" + ing.name + "」库存不够：这批要 " + need
                        + " 克，现库存只有 " + left + " 克");
            }
        }
    }
}
