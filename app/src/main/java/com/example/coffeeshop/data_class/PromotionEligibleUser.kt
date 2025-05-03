package com.example.coffeeshop.data_class

data class PromotionEligibleUser(
    val id: Int,
    val criteriaType: String,
    val operator: String,
    val criteriaValue: String
)
