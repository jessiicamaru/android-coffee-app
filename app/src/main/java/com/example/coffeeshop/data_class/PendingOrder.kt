package com.example.coffeeshop.data_class

import com.google.gson.annotations.SerializedName

data class PendingOrder(
    @SerializedName("orderId")
    val orderId: String,

    @SerializedName("user")
    val userInfo: UserInfo,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("stat")
    val stat: Int,

    @SerializedName("coffees")
    val coffees: List<Coffee>
)


