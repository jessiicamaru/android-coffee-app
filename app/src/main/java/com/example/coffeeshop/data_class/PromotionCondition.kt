package com.example.coffeeshop.data_class


data class PromotionCondition(
    val id: Int,
    val conditionType: String,
    val operator: String,
    val conditionValue: Double
)