package com.kitchen.central.entity;

import jakarta.persistence.*;

/** 厨具设备：编号唯一，归属某个操作间。 */
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 32)
    public String code;

    @Column(nullable = false, length = 64)
    public String name;

    /** 灶具 / 蒸箱 / 冷库 / 和面机 / 消毒柜 */
    @Column(nullable = false, length = 16)
    public String category;

    @Column(name = "kitchen_id")
    public Long kitchenId;

    /** 可用 / 停用 / 维修中 */
    @Column(nullable = false, length = 16)
    public String status;
}
