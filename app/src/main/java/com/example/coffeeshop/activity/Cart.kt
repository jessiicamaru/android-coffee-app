package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemCartAdapter
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.toast
import com.google.android.material.bottomnavigation.BottomNavigationView

class Cart : Activity() {
    private lateinit var buyNow: Button

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var coffeeRecyclerView: RecyclerView

    private lateinit var totalAmount: TextView

    private var store = Store.Companion.store;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart)

        store.dispatch(Action.AddHistory(this))

        buyNow = findViewById(R.id.buy_now)
        buyNow.setOnClickListener {
            if (store.state.orders.isEmpty()) {
                toast(this@Cart) {
                    "Your cart is empty"
                }
            } else {
                val intent = Intent(this, OnOrder::class.java)
                startActivity(intent)
            }

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )

        totalAmount = findViewById(R.id.totalAmount)


        coffeeRecyclerView = findViewById(R.id.cart_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        coffeeRecyclerView.setHasFixedSize(true)


        store.subscribe {
            runOnUiThread {
                updateUI(store.state)
            }
        }

        store.dispatch(Action.RefreshOrders)

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.nav_cart
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    overridePendingTransition(0, 0) // Tắt animation chuyển trang
                    return@setOnItemSelectedListener true
                }
                R.id.nav_cart -> return@setOnItemSelectedListener true
                R.id.nav_fav -> {
                    startActivity(Intent(this, Like::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_pending -> {
                    startActivity(Intent(this, Order::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, Account::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI", "Orders: ${state.orders}")

        val totalMoney = state.orders.sumOf { it.coffeeCost * it.quantity }

        totalAmount.text = "Total: ${String.format("%.2f", totalMoney).toDouble()}$"

        coffeeRecyclerView.adapter = CoffeeItemCartAdapter(ArrayList(state.orders), this)

    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "Cart received status for Order $orderId: $status")
            // TODO: Cập nhật UI hoặc chuyển màn hình

            toast(this@Cart) {
                "Your order status is changed"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
//        WebSocketManager.disconnect()
    }
}