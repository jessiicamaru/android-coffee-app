package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.FilterPromotionRequest
import com.example.coffeeshop.data_class.PromotionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PromotionApi {
    @GET("all-promotion")
    fun getAllPromotion(): Call<List<PromotionResponse>>

    @POST("filter-promotion")
    fun filterPromotion(@Body filterPromotionRequest: FilterPromotionRequest): Call<List<String>>
}