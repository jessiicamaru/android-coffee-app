package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.Likes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LikesApi {
    @POST("add-like-coffee")
    fun addLikeCoffee(@Body likes: Likes): Call<Int>
}