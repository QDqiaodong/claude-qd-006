package com.kitchen.central.controller;

import com.kitchen.central.entity.Dish;
import com.kitchen.central.service.MealService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final MealService service;

    public DishController(MealService service) {
        this.service = service;
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
}
