package com.example.coffeeshop.data_class

import com.google.gson.annotations.SerializedName

data class Likes(
    val uid: String,
    @SerializedName("coffee_id")
    val coffeeId: String
)
