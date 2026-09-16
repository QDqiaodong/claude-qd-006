package com.kitchen.central.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 备餐批次：一道菜在一个餐次做一批。 */
@Entity
@Table(name = "meal_batch")
public class MealBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "batch_no", nullable = false, unique = true, length = 32)
    public String batchNo;

    @Column(name = "dish_id", nullable = false)
    public Long dishId;

    @Column(name = "kitchen_id", nullable = false)
    public Long kitchenId;

    @Column(name = "serve_date", nullable = false)
    public LocalDate serveDate;

    /** 早餐 / 午餐 / 晚餐 */
    @Column(nullable = false, length = 16)
    public String meal;

    @Column(name = "plan_portions", nullable = false)
    public Integer planPortions;

    /** 实际做出来的份数 */
    @Column(name = "actual_portions", nullable = false)
    public Integer actualPortions;

    @Column(nullable = false, length = 32)
    public String chef;

    /** 留样克数，每批都要留 */
    @Column(name = "sample_weight")
    public Integer sampleWeight;

    /** 备料中 / 加工中 / 已完成 / 已作废 */
    @Column(nullable = false, length = 16)
    public String status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();
}
