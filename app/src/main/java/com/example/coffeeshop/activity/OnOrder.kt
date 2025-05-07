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
    private lateinit var proTotal: TextView
    private lateinit var ogTotal: TextView
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var orderButton: Button
    private lateinit var ogFee: TextView
    private lateinit var proFee: TextView
    private lateinit var returnButton: ImageButton
    private lateinit var openPromotion: GridLayout
    private lateinit var discountTV: TextView
    private val service = Service()
    private val store = Store.store
    private var shippingFee: Double = 1.0
    private var price: Double = 0.0
    private var distanceKm: Double? = null // Lưu khoảng cách
    private var geometry: JSONArray? = null // Lưu dữ liệu geometry từ getDistance
    private var originalTotal: Double = 0.0
    private var originalFee: Double = 0.0
    private var discountedTotal: Double = 0.0
    private var discountedFee: Double = 0.0

    @SuppressLint("SetTextI18n", "DefaultLocale", "MissingInflatedId")
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
        proTotal = findViewById(R.id.pro_total)
        ogTotal = findViewById(R.id.og_total)
        name = findViewById(R.id.name)
        address = findViewById(R.id.adress)
        orderButton = findViewById(R.id.order)
        ogFee = findViewById(R.id.og_fee)
        proFee = findViewById(R.id.pro_fee)
        discountTV = findViewById(R.id.discount_tv)

        orderButton.isClickable = false
        orderButton.isEnabled = false
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
            applyPromotions()
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
            applyPromotions()
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
            Log.d("OnOrder", "Order button clicked")
            if (store.state.user == null) {
                Log.e("OnOrder", "User is null, cannot create order")
                runOnUiThread {
                    toast(this) { "User not logged in" }
                }
                return@setOnClickListener
            }

            val user = store.state.user!!
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

            val webSocketManager = WebSocketManager.getInstance(this)
            webSocketManager.connectAndThen {
                runOnUiThread {
                    Log.d("OnOrder", "Inside connectAndThen")
                    if (store.state.address == null) {
                        Log.e("OnOrder", "Address is null, cannot create order")
                        toast(this@OnOrder) { "Address not set" }
                        return@runOnUiThread
                    }

                    val orderAddress = store.state.address!!
                    service.createOrder(
                        OrderRequest(
                            uid = user.uid,
                            coffees = coffeesToOrder,
                            orderId = orderId,
                            address = orderAddress,
                            total = discountedTotal,
                            fee = discountedFee,
                            longitude = store.state.location?.longitude ?: 0.0,
                            latitude = store.state.location?.latitude ?: 0.0,
                            note = "",
                            originalTotal = originalTotal,
                            originalFee = originalFee,
                            promotion = store.state.selectedPromotions
                        )
                    )

                    Log.d("OnOrder", "Before RemoveCart, Orders: ${store.state.orders}")
                    store.dispatch(Action.RemoveCart)
                    Log.d("OnOrder", "After RemoveCart, Orders: ${store.state.orders}")

                    Log.d("ORDER_ID", orderId)
                    val intent = Intent(this, Map::class.java).apply {
                        putExtra("stat", 0)
                        putExtra("fee", shippingFee)
                        putExtra("orderId", orderId)
                        putExtra("source", "OnOrder")
                    }
                    Log.d("OnOrder", "Starting Map activity")
                    startActivity(intent)
                    finish()
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
        discountTV.text = "${state.selectedPromotions.size} Discount is Applied"
        price = state.orders.sumOf { it.coffeeCost * it.quantity }
        applyPromotions()

        name.text = state.user?.displayName ?: "Jl. Kpg Sutoyo"
        address.text = state.address ?: "Kpg. Sutoyo No. 620, Bilzen, Tanjungbalai."

        coffeeRecyclerView.adapter = CoffeeItemOrderAdapter(ArrayList(state.orders), this)
    }

    private fun applyPromotions() {
        val df = DecimalFormat("#.##")

        // Tính giá trị ban đầu
        originalFee = if (distanceKm != null) distanceKm!! * 0.3 * 1.3 else 0.0 // Phí gốc (theo UI hiện tại)
        shippingFee = if (distanceKm != null) distanceKm!! * 0.3 else 0.0 // Phí cơ bản (trước khi áp dụng giảm giá)
        originalTotal = price + originalFee

        // Giá trị sau khi áp dụng promotion
        var discountedPrice = price
        discountedFee = shippingFee

        // Áp dụng promotion
        store.state.selectedPromotions.forEach { promotion ->
            when (promotion.promotionId) {
                "promo_001" -> { // SUMMER2025
                    // Giả định người dùng thỏa mãn orderCount > 10 (thiếu thông tin để kiểm tra)
                    if (price > 12.0) {
                        val discount = price * 0.20 // Giảm 20%
                        discountedPrice -= discount
                        Log.d("Promotion", "Applied SUMMER2025: Discounted price = $discountedPrice")
                    }
                }
                "promo_005" -> { // FREESHIP10KM
                    if (distanceKm != null && distanceKm!! < 10.0) {
                        discountedFee = 0.0 // Miễn phí vận chuyển
                        Log.d("Promotion", "Applied FREESHIP10KM: Discounted fee = $discountedFee")
                    }
                }
                "promo_006" -> { // NEWUSER15
                    // Giả định người dùng là newUser (thiếu thông tin để kiểm tra)
                    val discount = price * 0.15 // Giảm 15%
                    discountedPrice -= discount
                    Log.d("Promotion", "Applied NEWUSER15: Discounted price = $discountedPrice")
                }
            }
        }

        discountedTotal = discountedPrice + discountedFee

        // Cập nhật UI
        ogFee.text = "${df.format(originalFee)}$"
        proFee.text = "${df.format(discountedFee)}$"
        ogTotal.text = "${df.format(originalTotal)}$"
        proTotal.text = "${df.format(discountedTotal)}$"

        // Cập nhật trạng thái nút order
        runOnUiThread {
            orderButton.isClickable = true
            orderButton.isEnabled = true
            orderButton.setBackgroundResource(R.drawable.button_primary)
            orderButton.setTextColor(Color.WHITE)
        }
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

                    applyPromotions()
                }
            }
        })
    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "OnOrder received status for Order $orderId: $status")
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