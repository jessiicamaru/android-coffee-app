package com.example.coffeeshop.service

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.coffeeshop.api_interface.CategoryApi
import com.example.coffeeshop.api_interface.CoffeeApi
import com.example.coffeeshop.api_interface.LikesApi
import com.example.coffeeshop.api_interface.OrderApi
import com.example.coffeeshop.api_interface.PromotionApi
import com.example.coffeeshop.api_interface.UserApi
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.FilterPromotionRequest
import com.example.coffeeshop.data_class.Likes
import com.example.coffeeshop.data_class.OrderRequest
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.data_class.PromotionResponse
import com.example.coffeeshop.data_class.UpdateStatusPayload
import com.example.coffeeshop.data_class.User
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.utils.LocalDateTimeDeserializer
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

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

    fun deleteLikeCoffee(likes: Likes) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LikesApi::class.java)

        api.deleteLikeCoffee(likes.coffeeId, likes.uid).enqueue(object : Callback<Int> {
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

        api.getLikeCoffees(uid).enqueue(object : Callback<List<String>> {
            override fun onResponse(
                call: Call<List<String>>,
                response: Response<List<String>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("SHIT_FAV", "$it")

                        store.dispatch(Action.SetLikeCoffees(ArrayList(it)))
                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }
        })
    }

    fun getPendingOrder(uid: String) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(OrderApi::class.java)

        api.getPendingOrderByUid(uid).enqueue(object : Callback<List<PendingOrder>> {
            override fun onResponse(
                call: Call<List<PendingOrder>>,
                response: Response<List<PendingOrder>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("SHIT_FAV", "$it")
                        store.dispatch(Action.SetOrders(ArrayList(it)))
                    }
                }
            }

            override fun onFailure(call: Call<List<PendingOrder>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }
        })
    }

    fun createOrder(orderRequest: OrderRequest, callback: (Boolean) -> Unit) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(OrderApi::class.java)

        api.createOrder(orderRequest).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "create order successfully! Response: ${response.body()}")
                    callback(true);
                } else {
                    Log.e("Retrofit", "create order failed: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("Retrofit", "create order failed: ${t.message}")

            }
        })
    }

    fun updateOrderStatus(updateStatusPayload: UpdateStatusPayload) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(OrderApi::class.java)


        api.updateOrderStatus(updateStatusPayload).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "create order successfully! Response: ${response.body()}")
                } else {
                    Log.e("Retrofit", "create order failed: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("Retrofit", "create order failed: ${t.message}")

            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllPromotion(callback: (Boolean) -> Unit) {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .create()

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PromotionApi::class.java)

        api.getAllPromotion().enqueue(object : Callback<List<PromotionResponse>> {
            override fun onResponse(
                call: Call<List<PromotionResponse>>,
                response: Response<List<PromotionResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { promotions ->
                        store.dispatch(Action.SetPromotions(ArrayList(promotions)))
                        callback(true)
                    } ?: Log.w(TAG, "Response body is null")
                } else {
                    Log.e(TAG, "Response failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<PromotionResponse>>, t: Throwable) {
                Log.e(TAG, "On Fail: ${t.message}", t)
            }
        })
    }

    fun filterPromotion(filterPromotionRequest: FilterPromotionRequest, callback: (List<String>?) -> Unit) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PromotionApi::class.java)

        Log.d("filter", "Sending request: $filterPromotionRequest")

        api.filterPromotion(filterPromotionRequest).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        store.dispatch(Action.SetInvalidPromotions(ArrayList(it)))
                        Log.d("Retrofit", "Received invalid promotions: $it")
                        callback(it)
                    } ?: run {
                        Log.e("Retrofit", "Response body is null, code: ${response.code()}")
                        callback(null)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    Log.e("Retrofit", "Filter failed: ${response.code()} - $errorBody")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("Retrofit", "Filter error: ${t.message}", t)
                callback(null)
            }
        })
    }

}
