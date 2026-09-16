package com.kitchen.central.entity;

import jakarta.persistence.*;

/** 菜品配方：一道菜每做一份要耗某种原料多少克。 */
@Entity
@Table(name = "dish_ingredient")
public class DishIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "dish_id", nullable = false)
    public Long dishId;

    @Column(name = "ingredient_id", nullable = false)
    public Long ingredientId;

    /** 每份用量（克） */
    @Column(nullable = false)
    public Integer quantity;
}
