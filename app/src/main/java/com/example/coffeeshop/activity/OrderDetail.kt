package com.example.coffeeshop.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.adapter.CoffeeItemOrderPendingAdapter
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat

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

        val returnButton: ImageButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, Order::class.java)
            startActivity(intent)
        }

        coffeeRecyclerView = findViewById(R.id.order_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        coffeeRecyclerView.setHasFixedSize(true)

        store.subscribe {
            updateUI(store.state)
        }
        store.dispatch(Action.RefreshOrdersPending)

        val orderId = intent.getStringExtra("orderId")
        val orderPending = store.state.ordersPending.find { it.orderId == orderId }

        if (orderPending != null) {
            getMap(orderPending)
        }

        openMap.setOnClickListener {
            val intent = Intent(this, Map::class.java).apply {
                putExtra("orderId", orderId)
            }
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(state: AppState) {
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

        val stat = orderPending?.stat ?: 0

        when (stat) {
            0 -> status.text = "Pending"
            1 -> status.text = "Preparing"
            2 -> status.text = "Delivering"
            3 -> status.text = "Completed"
            else -> status.text = ""
        }

        coffeeRecyclerView.adapter = CoffeeItemOrderPendingAdapter(ArrayList(coffees), this, orderId ?: "")

    }

    private fun getMap(order: PendingOrder) {
        val url =
            "https://router.project-osrm.org/route/v1/driving/108.2561075672051,15.990506012096326;${order.longitude},${order.latitude}?geometries=geojson"

        Log.d("Route", url)

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Route", "Lỗi khi tải tuyến đường: ${e.message}")
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val jsonObject = JSONObject(responseString)
                    val routes = jsonObject.getJSONArray("routes")
                    val route = routes.getJSONObject(0)

                    val geometry = route.getJSONObject("geometry").getJSONArray("coordinates")

                    runOnUiThread {
                        store.dispatch(Action.SetMapData(geometry))
                    }
                }
            }
        })
    }
}