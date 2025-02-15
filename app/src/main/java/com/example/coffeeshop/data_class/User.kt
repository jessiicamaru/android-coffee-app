package com.example.coffeeshop.data_class

import com.google.gson.annotations.SerializedName

data class User(
    val uid: String,

    @SerializedName("display_name")
    val displayName: String,

    val email: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("photo_url")
    val photoUrl: String
)