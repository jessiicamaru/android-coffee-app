package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.Likes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LikesApi {
    @POST("add-like-coffee")
    fun addLikeCoffee(@Body likes: Likes): Call<Int>

    @GET("like-by-uid")
    fun getLikeCoffees(@Query("uid") uid: String): Call<List<String>>

    @DELETE("delete-by-coffee-id/{id}/{uid}")
    fun deleteLikeCoffee(@Path("id") id: String, @Path("uid") uid: String): Call<Int>
}