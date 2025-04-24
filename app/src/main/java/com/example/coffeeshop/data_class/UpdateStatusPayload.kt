package com.example.coffeeshop.data_class

data class UpdateStatusPayload(
    val userId: String,
    val orderId: String,
    val status: Int
)
