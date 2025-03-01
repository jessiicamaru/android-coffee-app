package com.example.coffeeshop.redux.data_class

import android.app.Activity
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.LocationData
import com.example.coffeeshop.data_class.User

data class AppState(
    val address: String? = null,
    val coffees: ArrayList<Coffee> = arrayListOf(),
    val categories: ArrayList<Category> = arrayListOf(),
    val selectedCategory: String? = "all",
    val orders: MutableList<Coffee> = arrayListOf(),
    val likeCoffees: ArrayList<String> = arrayListOf(),
    val historyList: ArrayList<Activity> = arrayListOf(),
    val user: User? = null,
    val location: LocationData? = null
)
