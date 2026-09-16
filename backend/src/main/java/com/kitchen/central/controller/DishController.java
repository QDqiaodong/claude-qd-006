package com.kitchen.central.controller;

import com.kitchen.central.entity.Dish;
import com.kitchen.central.entity.DishIngredient;
import com.kitchen.central.service.IngredientService;
import com.kitchen.central.service.MealService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final MealService service;
    private final IngredientService ingredientService;

    public DishController(MealService service, IngredientService ingredientService) {
        this.service = service;
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<Dish> list(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) String keyword) {
        return service.listDishes(status, category, keyword);
    }

    @PostMapping
    public Dish create(@RequestBody Dish input) {
        return service.createDish(input);
    }

    @PutMapping("/{id}")
    public Dish update(@PathVariable Long id, @RequestBody Dish input) {
        return service.updateDish(id, input);
    }

    /** 这道菜的配方：每份要耗哪些原料、各多少克。 */
    @GetMapping("/{id}/recipe")
    public List<DishIngredient> recipe(@PathVariable Long id) {
        return ingredientService.recipeOf(id);
    }

    @PutMapping("/{id}/recipe")
    public List<DishIngredient> saveRecipe(@PathVariable Long id, @RequestBody List<DishIngredient> lines) {
        return ingredientService.saveRecipe(id, lines);
    }
}
