package com.example.coffeeshop.service

import android.util.Log
import com.example.coffeeshop.api_interface.CategoryApi
import com.example.coffeeshop.api_interface.CoffeeApi
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Service {
    private var BASE_URL = "http://10.0.2.2:5000/"
    private var TAG = "DATA_RESPONSE"

    private var store = Store.Companion.store;

    fun getAllCoffees() {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CoffeeApi::class.java)

        api.getAllCoffees().enqueue(object : Callback<List<Coffee>> {
            override fun onResponse(call: Call<List<Coffee>>, response: Response<List<Coffee>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        store.dispatch(Action.SetCoffees(ArrayList(it)))
                    }
                }
            }

            override fun onFailure(call: Call<List<Coffee>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }
        })
    }

    fun getAllCategories() {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CategoryApi::class.java)

        api.getAllCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        store.dispatch(Action.SetCategories(ArrayList(it)))
                    }
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }
        })
    }
}