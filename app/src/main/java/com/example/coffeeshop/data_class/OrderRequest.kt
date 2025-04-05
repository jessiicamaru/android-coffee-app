package com.example.coffeeshop.data_class

data class OrderRequest(
    val uid: String,
    val orderId: String,
    val coffees: List<CoffeeRequest>,
    val address: String
)


