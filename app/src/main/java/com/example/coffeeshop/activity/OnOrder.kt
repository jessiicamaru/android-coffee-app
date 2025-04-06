package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.data_class.CoffeeRequest
import com.example.coffeeshop.data_class.OrderRequest
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.util.UUID

class OnOrder : Activity() {

    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var orderButton: Button
    private lateinit var ogFee: TextView
    private lateinit var proFee: TextView

    private val service = Service()
    private val store = Store.store
    private var shippingFee: Double = 0.0
    private var price: Double = 0.0

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_order)

        store.dispatch(Action.AddHistory(this))

        coffeeRecyclerView = findViewById(R.id.order_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        coffeeRecyclerView.setHasFixedSize(true)

        val returnButton: ImageButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, store.state.historyList.last()::class.java)
            startActivity(intent)
        }

        totalAmount = findViewById(R.id.total_amount)
        name = findViewById(R.id.name)
        address = findViewById(R.id.adress)
        orderButton = findViewById(R.id.order)
        ogFee = findViewById(R.id.og_fee)
        proFee = findViewById(R.id.pro_fee)

        orderButton.setOnClickListener {
            store.state.user?.let { user ->
                val coffeesToOrder = store.state.orders.map { coffee ->
                    CoffeeRequest(coffeeId = coffee.coffeeId, size = coffee.size, quantity = coffee.quantity)
                }
                val orderId = UUID.randomUUID().toString()
                val webSocketManager = WebSocketManager(user.uid)

                // Tính price trước khi gửi yêu cầu
                price = store.state.orders.sumOf { it.coffeeCost * it.quantity }
                Log.d("ORDER", "Price before sending: $price")

                webSocketManager.connectAndThen {
                    runOnUiThread {
                        store.state.address?.let { orderAddress ->
                            service.createOrder(
                                OrderRequest(
                                    uid = user.uid,
                                    coffees = coffeesToOrder,
                                    orderId = orderId,
                                    address = orderAddress,
                                    total = price, // Sử dụng price đã tính trước
                                    fee = shippingFee,
                                    longitude = store.state.location?.longitude ?: 0.0,
                                    latitude = store.state.location?.latitude ?: 0.0,
                                    note = ""
                                )
                            )
                        }

                        // Dispatch RemoveCart sau khi gửi yêu cầu
                        store.dispatch(Action.RemoveCart)

                        Log.d("ORDER_ID", orderId)
                        val intent = Intent(this, Map::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        val radioGroup = findViewById<RadioGroup>(R.id.delivery_group)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                for (i in 0 until group.childCount) {
                    val radioButton = group.getChildAt(i) as? RadioButton
                    radioButton?.let {
                        it.setBackgroundResource(R.drawable.radio_button)
                        if (it.id == checkedId) {
                            it.setTextColor(android.graphics.Color.WHITE)
                        } else {
                            it.setTextColor(android.graphics.Color.BLACK)
                        }
                    }
                }
            }
        }

        getDistance()

        store.subscribe {
            runOnUiThread {
                updateUI(store.state)
            }
        }

        store.dispatch(Action.RefreshOrders)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI_NOW", "Orders: ${state.orders}")

        price = state.orders.sumOf { it.coffeeCost * it.quantity }
        totalAmount.text = "${String.format("%.2f", price).toDouble()}$"
        name.text = state.user?.displayName ?: "Jl. Kpg Sutoyo"
        address.text = state.address ?: "Kpg. Sutoyo No. 620, Bilzen, Tanjungbalai."
        coffeeRecyclerView.adapter = CoffeeItemOrderAdapter(ArrayList(state.orders), this)
    }

    private fun getDistance() {
        val url =
            "https://router.project-osrm.org/route/v1/driving/108.2561075672051,15.990506012096326;${store.state.location?.longitude},${store.state.location?.latitude}?geometries=geojson"

        Log.d("Route", url)

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Route", "Lỗi khi tải tuyến đường: ${e.message}")
                runOnUiThread {
                    ogFee.text = "Error"
                    proFee.text = "Error"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val jsonObject = JSONObject(responseString)
                    val routes = jsonObject.getJSONArray("routes")
                    val route = routes.getJSONObject(0)

                    val distanceMeters = route.getDouble("distance")
                    val distanceKm = distanceMeters / 1000.0
                    shippingFee = distanceKm * 0.3

                    val df = DecimalFormat("#.##")
                    val formattedDistance = df.format(distanceKm)
                    val formattedFee = df.format(shippingFee)

                    Log.d("SHIPPING", "Distance: $formattedDistance km, Fee: $$formattedFee")

                    val geometry = route.getJSONObject("geometry").getJSONArray("coordinates")

                    runOnUiThread {
                        ogFee.text = "${df.format(shippingFee * 1.3)}$"
                        proFee.text = "$formattedFee$"
                        store.dispatch(Action.SetMapData(geometry))
                    }
                }
            }
        })
    }
}