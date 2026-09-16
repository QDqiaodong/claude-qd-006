package com.kitchen.central.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 出餐配送单：一批餐发给一个收货单位。 */
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "delivery_no", nullable = false, unique = true, length = 32)
    public String deliveryNo;

    @Column(name = "batch_id", nullable = false)
    public Long batchId;

    /** 收货单位 */
    @Column(nullable = false, length = 64)
    public String customer;

    @Column(nullable = false)
    public Integer portions;

    @Column(length = 32)
    public String driver;

    @Column(name = "deliver_date")
    public LocalDate deliverDate;

    /** 待发 / 在途 / 已签收 / 已退回 */
    @Column(nullable = false, length = 16)
    public String status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();
}
