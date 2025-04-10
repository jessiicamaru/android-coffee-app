package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CategoryItemAdapter
import com.example.coffeeshop.adapter.CoffeeItemAdapter
import com.example.coffeeshop.adapter.CoffeeItemCartAdapter
import com.example.coffeeshop.adapter.CoffeeItemLikeAdapter
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.google.android.material.bottomnavigation.BottomNavigationView

class Like : Activity() {
    private lateinit var coffeeRecyclerView: RecyclerView

    private lateinit var bottomNavigationView: BottomNavigationView

    private var store = Store.Companion.store;
    private val service = Service();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.like)

        store.dispatch(Action.AddHistory(this))


        coffeeRecyclerView = findViewById(R.id.cart_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        coffeeRecyclerView.setHasFixedSize(true)

        Log.d("store.state.user?.uid?", "${store.state.user?.uid}")

        store.state.user?.uid?.let {
            service.getLikeCoffees(it)
        }

        store.subscribe {
            updateUI(store.state)
        }

        store.dispatch(Action.RefreshOrders);


        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.nav_fav
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_pending -> {
                    startActivity(Intent(this, Order::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_fav -> return@setOnItemSelectedListener true
                else -> false
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI", "FAV: ${state.likeCoffees}")

        val list = state.coffees.filter { coffee ->  state.likeCoffees.find { it == coffee.coffeeId } != null}

        coffeeRecyclerView.adapter = CoffeeItemLikeAdapter(ArrayList(list), this)

    }

}