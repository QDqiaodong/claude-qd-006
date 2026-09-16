package com.kitchen.central.controller;

import com.kitchen.central.entity.MealBatch;
import com.kitchen.central.service.MealService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final MealService service;

    public BatchController(MealService service) {
        this.service = service;
    }

    @GetMapping
    public List<MealBatch> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long kitchenId,
            @RequestParam(required = false) String keyword) {
        return service.listBatches(status, date, kitchenId, keyword);
    }

    @PostMapping
    public MealBatch open(@RequestBody MealBatch input) {
        return service.open(input);
    }

    @PostMapping("/{id}/advance")
    public MealBatch advance(@PathVariable Long id,
                             @RequestParam String action,
                             @RequestParam(required = false) Integer actualPortions) {
        return service.advance(id, action, actualPortions);
    }
}
