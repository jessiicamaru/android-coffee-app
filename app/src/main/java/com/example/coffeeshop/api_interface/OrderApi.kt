package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.OrderRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApi {

    @POST("create-order")
    fun createOrder(@Body orderRequest: OrderRequest): Call<Int>
}