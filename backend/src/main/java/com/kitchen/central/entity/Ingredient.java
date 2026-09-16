package com.kitchen.central.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/** 原料台账：记下名称、当前库存和保质期，过期不能耗。 */
@Entity
@Table(name = "ingredient")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 原料名称，台账里不重名 */
    @Column(nullable = false, unique = true, length = 64)
    public String name;

    /** 当前库存（克） */
    @Column(nullable = false)
    public Integer stock;

    /** 保质期到哪天，早于今天就算过期 */
    @Column(name = "expiry_date", nullable = false)
    public LocalDate expiryDate;
}
