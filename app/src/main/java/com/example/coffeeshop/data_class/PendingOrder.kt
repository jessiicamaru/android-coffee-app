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

    @SerializedName("total")
    val total: Double,

    @SerializedName("fee")
    val fee: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("note")
    val note: String,

    @SerializedName("coffees")
    val coffees: List<Coffee>
)


