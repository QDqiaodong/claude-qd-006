package com.kitchen.central.entity;

import jakarta.persistence.*;

/** 菜品：编号唯一，停用后不再排新批次。 */
@Entity
@Table(name = "dish")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 32)
    public String code;

    @Column(nullable = false, length = 64)
    public String name;

    /** 荤菜 / 素菜 / 汤 / 主食 */
    @Column(nullable = false, length = 16)
    public String category;

    /** 在售 / 停用 */
    @Column(nullable = false, length = 16)
    public String status;
}
