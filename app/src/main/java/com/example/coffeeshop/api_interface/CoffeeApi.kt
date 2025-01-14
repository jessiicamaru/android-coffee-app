package com.example.coffeeshop.api_interface

import retrofit2.Call
import com.example.coffeeshop.data_class.Coffee
import retrofit2.http.GET

interface CoffeeApi {
    @GET("all-coffee")
    fun getAllCoffees(): Call<List<Coffee>>
}