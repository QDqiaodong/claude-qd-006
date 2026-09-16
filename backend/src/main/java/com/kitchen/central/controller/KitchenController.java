package com.kitchen.central.controller;

import com.kitchen.central.entity.Equipment;
import com.kitchen.central.entity.Kitchen;
import com.kitchen.central.service.KitchenService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class KitchenController {

    private final KitchenService service;

    public KitchenController(KitchenService service) {
        this.service = service;
    }

    @GetMapping("/kitchens")
    public List<Kitchen> listKitchens(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String keyword) {
        return service.listKitchens(status, keyword);
    }

    @PostMapping("/kitchens")
    public Kitchen createKitchen(@RequestBody Kitchen input) {
        return service.createKitchen(input);
    }

    @PutMapping("/kitchens/{id}")
    public Kitchen updateKitchen(@PathVariable Long id, @RequestBody Kitchen input) {
        return service.updateKitchen(id, input);
    }

    @GetMapping("/equipments")
    public List<Equipment> listEquipments(@RequestParam(required = false) Long kitchenId,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String keyword) {
        return service.listEquipments(kitchenId, status, keyword);
    }

    @PostMapping("/equipments")
    public Equipment createEquipment(@RequestBody Equipment input) {
        return service.createEquipment(input);
    }

    @PutMapping("/equipments/{id}")
    public Equipment updateEquipment(@PathVariable Long id, @RequestBody Equipment input) {
        return service.updateEquipment(id, input);
    }
}
