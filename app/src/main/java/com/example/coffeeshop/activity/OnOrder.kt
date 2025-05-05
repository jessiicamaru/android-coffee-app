package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.data_class.CoffeeRequest
import com.example.coffeeshop.data_class.OrderRequest
import com.example.coffeeshop.data_class.FilterPromotionRequest
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.util.UUID
import java.util.concurrent.TimeUnit

class OnOrder : Activity() {

    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var orderButton: Button
    private lateinit var ogFee: TextView
    private lateinit var proFee: TextView
    private lateinit var returnButton: ImageButton
    private lateinit var openPromotion: GridLayout
    private val service = Service()
    private val store = Store.store
    private var shippingFee: Double = 1.0
    private var price: Double = 0.0
    private var distanceKm: Double? = null // Lưu khoảng cách
    private var geometry: JSONArray? = null // Lưu dữ liệu geometry từ getDistance

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_order)

        store.dispatch(Action.AddHistory(this))

        coffeeRecyclerView = findViewById(R.id.order_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        coffeeRecyclerView.setHasFixedSize(true)

        returnButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        openPromotion = findViewById(R.id.open_promotion)
        totalAmount = findViewById(R.id.total_amount)
        name = findViewById(R.id.name)
        address = findViewById(R.id.adress)
        orderButton = findViewById(R.id.order)
        ogFee = findViewById(R.id.og_fee)
        proFee = findViewById(R.id.pro_fee)

        orderButton.isClickable = false;
        orderButton.isEnabled = false;
        orderButton.setBackgroundResource(R.drawable.button_non_primary)
        orderButton.setTextColor(Color.BLACK)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )

        if (store.state.shippingFee != null && store.state.distanceKm != null) {
            shippingFee = store.state.shippingFee!!
            distanceKm = store.state.distanceKm!!
            geometry = store.state.mapData
Log.d("Route", "1")
            val df = DecimalFormat("#.##")
            ogFee.text = "${df.format(shippingFee * 1.3)}$"
            proFee.text = "${df.format(shippingFee)}$"
            orderButton.isClickable = true
            orderButton.isEnabled = true
            orderButton.setBackgroundResource(R.drawable.button_primary)
            orderButton.setTextColor(Color.WHITE)
        } else if (savedInstanceState != null) {
            Log.d("Route", "2")
            shippingFee = savedInstanceState.getDouble("shippingFee", 1.0)
            price = savedInstanceState.getDouble("price", 0.0)
            distanceKm = savedInstanceState.getDouble("distanceKm", -1.0).takeIf { it != -1.0 }
            val geometryString = savedInstanceState.getString("geometry")
            if (geometryString != null) {
                geometry = JSONArray(geometryString)
                store.dispatch(Action.SetMapData(geometry!!))
            }

            val df = DecimalFormat("#.##")
            ogFee.text = "${df.format(shippingFee * 1.3)}$"
            proFee.text = "${df.format(shippingFee)}$"
            orderButton.isClickable = true
            orderButton.isEnabled = true
            orderButton.setBackgroundResource(R.drawable.button_primary)
            orderButton.setTextColor(Color.WHITE)
        } else {
            getDistance()
        }

        openPromotion.setOnClickListener {
            Log.d("OpenPromotion", "1")
            service.filterPromotion(FilterPromotionRequest(
                uid = store.state.user?.uid ?: "",
                totalAmount = price,
                distance = shippingFee / 0.3,
                itemCount = store.state.orders.sumOf { coffee -> coffee.quantity }
            )) {
                val intent = Intent(this, Promotion::class.java)
                startActivity(intent)
            }
        }

        orderButton.setOnClickListener {
            store.state.user?.let { user ->
                val coffeesToOrder = store.state.orders.map { coffee ->
                    CoffeeRequest(
                        coffeeId = coffee.coffeeId,
                        size = coffee.size,
                        quantity = coffee.quantity
                    )
                }
                val orderId = UUID.randomUUID().toString()

                price = store.state.orders.sumOf { it.coffeeCost * it.quantity }
                Log.d("ORDER", "Price before sending: $price")

                // Sử dụng instance WebSocketManager đã có
                val webSocketManager = WebSocketManager.getInstance(this)
                webSocketManager.connectAndThen {
                    runOnUiThread {
                        store.state.address?.let { orderAddress ->
                            service.createOrder(
                                OrderRequest(
                                    uid = user.uid,
                                    coffees = coffeesToOrder,
                                    orderId = orderId,
                                    address = orderAddress,
                                    total = price,
                                    fee = shippingFee,
                                    longitude = store.state.location?.longitude ?: 0.0,
                                    latitude = store.state.location?.latitude ?: 0.0,
                                    note = "",
                                    originalTotal = 1.0,
                                    originalFee = 1.0,
                                    promotion = arrayListOf()
                                )
                            )
                        }

                        // Dispatch RemoveCart sau khi gửi yêu cầu
                        store.dispatch(Action.RemoveCart)


                        Log.d("ORDER_ID", orderId)
                        val intent = Intent(this, Map::class.java).apply {
                            putExtra("stat", 0)
                            putExtra("fee", shippingFee)
                            putExtra("orderId", orderId)
                            putExtra("source", "OnOrder")
                        }
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
                            it.setTextColor(Color.WHITE)
                        } else {
                            it.setTextColor(Color.BLACK)
                        }
                    }
                }
            }
        }

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

        if (state.orders.isEmpty()) {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            this.finish()
            return
        }

        coffeeRecyclerView.adapter = CoffeeItemOrderAdapter(ArrayList(state.orders), this)
    }

    private fun getDistance(retryCount: Int = 0, maxRetries: Int = 3) {
        val url =
            "https://router.project-osrm.org/route/v1/driving/108.2561075672051,15.990506012096326;${store.state.location?.longitude},${store.state.location?.latitude}?geometries=geojson"

        Log.d("Route", url)

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Route", "Lỗi khi tải tuyến đường: ${e.message}")
                if (retryCount < maxRetries) {
                    Log.d("Route", "Thử lại lần ${retryCount + 1}/$maxRetries")
                    getDistance(retryCount + 1, maxRetries)
                } else {
                    runOnUiThread {
                        ogFee.text = "Error"
                        proFee.text = "Error"
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                Log.d("Mapshit", "1")
                runOnUiThread {
                    orderButton.isClickable = true
                    orderButton.isEnabled = true
                    orderButton.setBackgroundResource(R.drawable.button_primary)
                    orderButton.setTextColor(Color.WHITE)
                }
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val jsonObject = JSONObject(responseString)
                    val routes = jsonObject.getJSONArray("routes")
                    val route = routes.getJSONObject(0)

                    distanceKm = route.getDouble("distance") / 1000.0
                    shippingFee = distanceKm!! * 0.3

                    val df = DecimalFormat("#.##")
                    val formattedDistance = df.format(distanceKm)
                    val formattedFee = df.format(shippingFee)

                    Log.d("SHIPPING", "Distance: $formattedDistance km, Fee: $$formattedFee")

                    geometry = route.getJSONObject("geometry").getJSONArray("coordinates")

                    store.dispatch(Action.SetShippingData(shippingFee, distanceKm!!))
                    store.dispatch(Action.SetMapData(geometry!!))

                    runOnUiThread {
                        ogFee.text = "${df.format(shippingFee * 1.3)}$"
                        proFee.text = "$formattedFee$"
                    }
                }
            }
        })
    }
    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "OnOrder received status for Order $orderId: $status")
            // TODO: Cập nhật UI hoặc chuyển màn hình

            runOnUiThread {
                toast(this@OnOrder) {
                    "Your order status is changed"
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("shippingFee", shippingFee)
        outState.putDouble("price", price)
        outState.putDouble("distanceKm", distanceKm ?: -1.0)
        geometry?.let { outState.putString("geometry", it.toString()) }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("OnOrder", "Activity destroyed")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
        WebSocketManager.getInstance(this).disconnect()
    }
}