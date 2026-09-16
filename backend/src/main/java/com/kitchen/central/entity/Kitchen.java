package com.kitchen.central.entity;

import jakarta.persistence.*;

/** 操作间：编号唯一。 */
@Entity
@Table(name = "kitchen")
public class Kitchen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 32)
    public String code;

    @Column(nullable = false, length = 64)
    public String name;

    /** 粗加工 / 热厨 / 凉菜 / 面点 / 洗消 */
    @Column(nullable = false, length = 16)
    public String kind;

    /** 在用 / 停用 / 维修 */
    @Column(nullable = false, length = 16)
    public String status;
}
