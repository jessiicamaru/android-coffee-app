package com.example.coffeeshop.data_class

data class OrderRequest(
    val uid: String,
    val orderId: String,
    val coffees: List<CoffeeRequest>,
    val address: String,
    val total: Double,
    val fee: Double,
    val originalTotal: Double,
    val originalFee: Double,
    val longitude: Double,
    val latitude: Double,
    val note: String,
    val promotion: List<Promotion> ?= listOf(),
)


