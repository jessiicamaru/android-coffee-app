package com.example.coffeeshop.redux.data_class

import android.app.Activity
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.LocationData
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.data_class.Promotion
import com.example.coffeeshop.data_class.PromotionResponse
import com.example.coffeeshop.data_class.SocketResponse
import com.example.coffeeshop.data_class.User
import org.json.JSONArray

data class AppState(
    val address: String? = null,
    val coffees: ArrayList<Coffee> = arrayListOf(),
    val categories: ArrayList<Category> = arrayListOf(),
    val ordersPending: ArrayList<PendingOrder> = arrayListOf(),
    val selectedCategory: String? = "all",
    val orders: MutableList<Coffee> = arrayListOf(),
    val likeCoffees: ArrayList<String> = arrayListOf(),
    val historyList: ArrayList<Activity> = arrayListOf(),
    val user: User? = null,
    val mapData: JSONArray? = null,
    val location: LocationData? = null,
    val promotions: ArrayList<PromotionResponse> = arrayListOf(),
    val invalidPromotions:  ArrayList<String> = arrayListOf(),
    val selectedPromotions: ArrayList<Promotion> = arrayListOf(),
    val notifications: ArrayList<SocketResponse> = arrayListOf(),
    val shippingFee: Double? = null,
    val distanceKm: Double? = null
)
