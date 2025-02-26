package com.example.coffeeshop.service

import android.util.Log
import com.example.coffeeshop.api_interface.CategoryApi
import com.example.coffeeshop.api_interface.CoffeeApi
import com.example.coffeeshop.api_interface.LikesApi
import com.example.coffeeshop.api_interface.UserApi
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.Likes
import com.example.coffeeshop.data_class.User
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

    fun postToSaveUser(user: User, callback: (Boolean) -> Unit) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserApi::class.java)

        api.saveUser(user).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    store.dispatch(Action.SaveUser(user))
                    Log.d("Retrofit", "User saved successfully! Response: ${response.body()}")
                    callback(true)
                } else {
                    Log.e("Retrofit", "Request failed: ${response.errorBody()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("Retrofit", "Request failed: ${t.message}")
                callback(false)
            }
        })
    }

    fun addLikeCoffee(likes: Likes) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LikesApi::class.java)

        api.addLikeCoffee(likes).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "Add coffee successfully! Response: ${response.body()}")
                } else {
                    Log.e("Retrofit", "Add coffee failed: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("Retrofit", "Add coffee failed: ${t.message}")

            }
        })
    }

    fun deleteLikeCoffee(id: String, uid: String) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LikesApi::class.java)

        Log.d("DELETE_FAV", id)

        api.deleteLikeCoffee(id, uid).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "Add coffee successfully! Response: ${response.body()}")
                } else {
                    Log.e("Retrofit", "Add coffee failed: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("Retrofit", "Add coffee failed: ${t.message}")

            }
        })
    }

    fun getLikeCoffees(uid: String) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LikesApi::class.java)

        Log.d("SHIT_FAV", uid)

        api.getLikeCoffees(uid).enqueue(object : Callback<List<Coffee>> {
            override fun onResponse(
                call: Call<List<Coffee>>,
                response: Response<List<Coffee>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        store.dispatch(Action.SetLikeCoffees(ArrayList(it)))
                        Log.d("SHIT_FAV", "$it")
                    }
                }
            }

            override fun onFailure(call: Call<List<Coffee>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }
        })
    }

}
