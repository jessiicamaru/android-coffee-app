package com.example.coffeeshop.data_class

data class CoffeeCart(
    val coffeeId: String,
    val coffeeTitle: String,
    val coffeePhotoUrl: String,
    val coffeeCost: Double,
    val coffeeDescription: String,
    val categoryTitle: String,
    val categoryId: String,
    val amount: Int
)