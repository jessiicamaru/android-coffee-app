package com.example.coffeeshop.redux.data_class

import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee

data class AppState(
    val coffees: ArrayList<Coffee> = arrayListOf(),
    val categories: ArrayList<Category> = arrayListOf(),
    val selectedCategory: String? = "all",
    val orders: ArrayList<Coffee> = arrayListOf()
)
