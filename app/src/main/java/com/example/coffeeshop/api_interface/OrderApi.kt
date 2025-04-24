package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.OrderRequest
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.data_class.UpdateStatusPayload
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    @POST("create-order")
    fun createOrder(@Body orderRequest: OrderRequest): Call<Int>

    @GET("get-pending-orders")
    fun getPendingOrderByUid(@Query("uid") uid: String): Call<List<PendingOrder>>

    @POST("update-order-status")
    fun updateOrderStatus(@Body updateStatusPayload: UpdateStatusPayload): Call<String>
}