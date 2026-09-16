package com.kitchen.central.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 操作间排期：哪个班组在哪个操作间的哪一段干活。 */
@Entity
@Table(name = "cooking_slot")
public class CookingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "slot_no", nullable = false, unique = true, length = 32)
    public String slotNo;

    @Column(name = "kitchen_id", nullable = false)
    public Long kitchenId;

    @Column(name = "serve_date", nullable = false)
    public LocalDate serveDate;

    /** 早餐 / 午餐 / 晚餐 */
    @Column(nullable = false, length = 16)
    public String meal;

    /** 班组 */
    @Column(nullable = false, length = 32)
    public String team;

    @Column(name = "start_min", nullable = false)
    public Integer startMin;

    @Column(name = "end_min", nullable = false)
    public Integer endMin;

    /** 待开始 / 进行中 / 已完成 / 已取消 */
    @Column(nullable = false, length = 16)
    public String status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();
}
