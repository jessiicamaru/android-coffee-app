package com.example.coffeeshop.data_class

data class PromotionDiscount(
    val id: Int,
    val discountTarget: String,
    val discountType: String,
    val discountValue: Double
)
