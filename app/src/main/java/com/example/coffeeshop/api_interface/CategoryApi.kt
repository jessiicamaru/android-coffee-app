package com.example.coffeeshop.api_interface

import com.example.coffeeshop.data_class.Category
import retrofit2.Call
import retrofit2.http.GET

interface CategoryApi {
    @GET("all-category")
    fun getAllCategories(): Call<List<Category>>
}