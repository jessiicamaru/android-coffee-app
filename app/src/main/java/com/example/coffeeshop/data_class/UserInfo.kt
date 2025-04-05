package com.example.coffeeshop.data_class

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String
)

