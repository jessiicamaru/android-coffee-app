package com.example.coffeeshop.data_class


data class Coffee(
    val coffeeId: String,
    val coffeeTitle: String,
    val coffeePhotoUrl: String,
    val coffeeCost: Double,
    val coffeeDescription: String,
    val categoryTitle: String,
    val categoryId: String,
    var quantity: Int = 1,
    var size: String = "M",
    var fav: Boolean = false
)