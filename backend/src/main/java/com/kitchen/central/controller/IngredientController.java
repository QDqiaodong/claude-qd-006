package com.kitchen.central.controller;

import com.kitchen.central.entity.Ingredient;
import com.kitchen.central.service.IngredientService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService service;

    public IngredientController(IngredientService service) {
        this.service = service;
    }

    @GetMapping
    public List<Ingredient> list(@RequestParam(required = false) String keyword) {
        return service.list(keyword);
    }

    @PostMapping
    public Ingredient create(@RequestBody Ingredient input) {
        return service.create(input);
    }

    @PutMapping("/{id}")
    public Ingredient update(@PathVariable Long id, @RequestBody Ingredient input) {
        return service.update(id, input);
    }
}
