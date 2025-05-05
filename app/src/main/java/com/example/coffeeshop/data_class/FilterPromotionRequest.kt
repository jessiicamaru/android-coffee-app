package com.example.coffeeshop.data_class

data class FilterPromotionRequest(
    val uid: String,
    val totalAmount: Double,
    val distance: Double,
    val itemCount: Int
)
