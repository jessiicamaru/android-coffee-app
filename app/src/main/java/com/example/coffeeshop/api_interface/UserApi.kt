package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("save-user")
    fun saveUser(@Body user: User): Call<Int>
}