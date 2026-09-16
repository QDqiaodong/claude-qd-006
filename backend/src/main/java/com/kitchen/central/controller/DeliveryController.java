package com.kitchen.central.controller;

import com.kitchen.central.dto.BatchQuota;
import com.kitchen.central.entity.Delivery;
import com.kitchen.central.service.DeliveryService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Delivery> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) Long batchId) {
        return service.list(status, batchId);
    }

    /** 各批次还能发多少份，以后台口径为准，前台开单前都来这里对 */
    @GetMapping("/quota")
    public List<BatchQuota> quotas() {
        return service.quotas();
    }

    @PostMapping
    public Delivery open(@RequestBody Delivery input) {
        return service.open(input);
    }

    @PostMapping("/{id}/advance")
    public Delivery advance(@PathVariable Long id,
                            @RequestParam String action,
                            @RequestParam(required = false) String driver,
                            @RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliverDate) {
        return service.advance(id, action, driver, deliverDate);
    }
}
