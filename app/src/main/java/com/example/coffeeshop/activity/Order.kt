package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.OrderAdapter
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Order : Activity() {
    private lateinit var todayRecyclerView: RecyclerView
    private lateinit var beforeRecyclerView: RecyclerView

    private lateinit var bottomNavigationView: BottomNavigationView

    private var store = Store.Companion.store;
    private val service = Service();

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.orders)

        store.dispatch(Action.AddHistory(this))

        store.state.user?.let { service.getPendingOrder(it.uid) }

        store.subscribe {
            updateUI(store.state)
        }

//        store.dispatch(Action.RefreshOrders);

        todayRecyclerView = findViewById(R.id.today_orders)
        todayRecyclerView.layoutManager = GridLayoutManager(this, 1)
        todayRecyclerView.setHasFixedSize(true)

        beforeRecyclerView = findViewById(R.id.before_orders)
        beforeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        beforeRecyclerView.setHasFixedSize(true)

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.nav_pending
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_fav -> {
                    startActivity(Intent(this, Like::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_pending -> return@setOnItemSelectedListener true
                else -> false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

        Log.d("OrderActivity", "Orders Pending: ${state.ordersPending}")

        val orders = state.ordersPending ?: emptyList()

        val todayOrders = orders.filter {
            val orderDateTime = LocalDateTime.parse(it.createdAt, formatter)
            val orderDate = orderDateTime.toLocalDate()
            orderDate.isEqual(today)
        }
        todayRecyclerView.adapter = OrderAdapter(todayOrders, this)

        val beforeOrders = orders.filter {
            val orderDateTime = LocalDateTime.parse(it.createdAt, formatter)
            val orderDate = orderDateTime.toLocalDate()
            orderDate.isBefore(today)
        }
        beforeRecyclerView.adapter = OrderAdapter(beforeOrders, this)
    }

}