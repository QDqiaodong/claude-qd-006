package com.kitchen.central.controller;

import com.kitchen.central.entity.CookingSlot;
import com.kitchen.central.service.SlotService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final SlotService service;

    public SlotController(SlotService service) {
        this.service = service;
    }

    @GetMapping
    public List<CookingSlot> list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long kitchenId,
            @RequestParam(required = false) String team) {
        return service.list(date, status, kitchenId, team);
    }

    @PostMapping
    public CookingSlot open(@RequestBody CookingSlot input) {
        return service.open(input);
    }

    @PostMapping("/{id}/advance")
    public CookingSlot advance(@PathVariable Long id, @RequestParam String action) {
        return service.advance(id, action);
    }
}
