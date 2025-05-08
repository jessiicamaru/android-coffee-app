package com.example.coffeeshop.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.adapter.CoffeeItemOrderPendingAdapter
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.data_class.SocketResponse
import com.example.coffeeshop.data_class.UpdateStatusPayload
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
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class OrderDetail : Activity() {
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var status: TextView
    private lateinit var totalAmount: TextView
    private lateinit var orderIdTV: TextView
    private lateinit var openMap: Button
    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var ogFee: TextView
    private lateinit var proFee: TextView
    private lateinit var orderArrived: Button
    private var currentOrderId: String? = null;
    private val service = Service();
    private val store = Store.store

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_detail)

        store.dispatch(Action.AddHistory(this))

        address = findViewById(R.id.address)
        totalAmount = findViewById(R.id.total_amount)
        name = findViewById(R.id.name)
        openMap = findViewById(R.id.open_map)
        status = findViewById(R.id.status)
        orderIdTV = findViewById(R.id.order_id)
        ogFee = findViewById(R.id.og_fee)
        proFee = findViewById(R.id.pro_fee)
        orderArrived = findViewById(R.id.order_arrived)

        val returnButton: ImageButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, Order::class.java)
            startActivity(intent)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )

        coffeeRecyclerView = findViewById(R.id.order_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        coffeeRecyclerView.setHasFixedSize(true)

        store.subscribe {
            runOnUiThread {
                updateUI(store.state, null)
            }
        }
        store.dispatch(Action.RefreshOrdersPending)

        currentOrderId = intent.getStringExtra("orderId")
        val orderPending = store.state.ordersPending.find { it.orderId == currentOrderId }

        if (orderPending != null) {
            if (orderPending.stat <= 3)
                getMap(orderPending)
        }

        openMap.setBackgroundResource(R.drawable.button_non_primary)
        openMap.setTextColor(Color.BLACK)
        openMap.isEnabled = false
        openMap.isClickable = false

        openMap.setOnClickListener {
            val intent = Intent(this, Map::class.java).apply {
                putExtra("orderId", currentOrderId)
                putExtra("source", "OrderDetail")
            }
            startActivity(intent)
        }

        orderArrived.setOnClickListener {
            runOnUiThread {
                service.updateOrderStatus(UpdateStatusPayload(
                    userId = store.state.user?.uid ?: "",
                    orderId = currentOrderId ?: "",
                    status = 3,
                ))
            }
        }
    }



    @SuppressLint("SetTextI18n")
    private fun updateUI(state: AppState, statRes: Int?) {
        val orderId = intent.getStringExtra("orderId")
        val orderPending = state.ordersPending.find { it.orderId == orderId }
        Log.d("UPDATED", "$orderPending")

        val coffees = orderPending?.coffees ?: arrayListOf();

        name.text = orderPending?.userInfo?.name ?: "";
        address.text = orderPending?.address;
        orderIdTV.text = orderId;

        val df = DecimalFormat("#.##")

        totalAmount.text = "${df.format(orderPending?.total)}$"
        ogFee.text = "${df.format(orderPending?.fee?.times(1.3))}$"
        proFee.text = "${df.format(orderPending?.fee)}$"

        val stat = statRes?: orderPending?.stat ?: 0

        when (stat) {
            0 -> {
                status.text = "Pending"
                orderArrived.visibility = View.INVISIBLE
            }

            1 -> {
                status.text = "Preparing"
                status.setTextColor(Color.parseColor("#FFA955"))
                orderArrived.visibility = View.INVISIBLE
            }

            2 -> {
                status.text = "Delivering"
                status.setTextColor(Color.parseColor("#C67C4E"))
                orderArrived.visibility = View.VISIBLE
                openMap.setTextColor(Color.BLACK)
                openMap.setBackgroundResource(R.drawable.button_non_primary)
            }

            3 -> {
                status.text = "Success"
                status.setTextColor(Color.parseColor("#36C07E"))
                orderArrived.visibility = View.INVISIBLE
                openMap.visibility = View.INVISIBLE
            }

            4 -> {
                status.text = "Cancelled"
                status.setTextColor(Color.parseColor("#CF0F47"))
                orderArrived.visibility = View.INVISIBLE
                openMap.visibility = View.INVISIBLE
            }
            else -> status.text = ""
        }

        coffeeRecyclerView.adapter = CoffeeItemOrderPendingAdapter(ArrayList(coffees), this, orderId ?: "")
    }

    private fun getMap(order: PendingOrder, retryCount: Int = 0, maxRetries: Int = 3) {
        val url =
            "https://router.project-osrm.org/route/v1/driving/108.2561075672051,15.990506012096326;${order.longitude},${order.latitude}?geometries=geojson"

        Log.d("Route", url)

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Route", "Lỗi khi tải tuyến đường: ${e.message}")
                if (retryCount < maxRetries) {
                    Log.d("Route", "Thử lại lần ${retryCount + 1}/$maxRetries")
                    getMap(order,retryCount + 1, maxRetries) // Thử lại
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val jsonObject = JSONObject(responseString)
                    val routes = jsonObject.getJSONArray("routes")
                    val route = routes.getJSONObject(0)
                    Log.d("Route", "DM")
                    val geometry = route.getJSONObject("geometry").getJSONArray("coordinates")

                    runOnUiThread {
                        openMap.setBackgroundResource(R.drawable.button_primary)
                        openMap.setTextColor(Color.WHITE)
                        openMap.isEnabled = true
                        openMap.isClickable = true

                        store.dispatch(Action.SetMapData(geometry))
                    }
                }
            }
        })
    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "OrderDetail received status for Order $orderId: $status")
            // TODO: Cập nhật UI hoặc chuyển màn hình


            runOnUiThread {
                if (currentOrderId != null && orderId == currentOrderId) {
                    updateUI(store.state, status)
                }
                toast(this@OrderDetail) {
                    "Your order status is changed"
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
//        WebSocketManager.disconnect()
    }
}