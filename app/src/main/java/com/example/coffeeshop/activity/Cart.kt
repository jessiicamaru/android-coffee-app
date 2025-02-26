package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CategoryItemAdapter
import com.example.coffeeshop.adapter.CoffeeItemAdapter
import com.example.coffeeshop.adapter.CoffeeItemCartAdapter
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store

class Cart : Activity() {
    private lateinit var homeButton: LinearLayout
    private lateinit var heartButton: LinearLayout
    private lateinit var buyNow: Button

    private lateinit var coffeeRecyclerView: RecyclerView

    private lateinit var totalAmount: TextView

    private var store = Store.Companion.store;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart)

        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        heartButton = findViewById(R.id.heartButton)
        heartButton.setOnClickListener {
            val intent = Intent(this, Like::class.java)
            startActivity(intent)
        }

        buyNow = findViewById(R.id.buy_now)
        buyNow.setOnClickListener {
            val intent = Intent(this, Like::class.java)
            startActivity(intent)
        }


        totalAmount = findViewById(R.id.totalAmount)


        coffeeRecyclerView = findViewById(R.id.cart_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        coffeeRecyclerView.setHasFixedSize(true)


        store.subscribe {
            updateUI(store.state)
        }

        store.dispatch(Action.RefreshOrders)

    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI", "Orders: ${state.orders}")

        val totalMoney = state.orders.sumOf { it.coffeeCost * it.quantity }

        totalAmount.text = "Total: ${String.format("%.2f", totalMoney).toDouble()}$"

        coffeeRecyclerView.adapter = CoffeeItemCartAdapter(ArrayList(state.orders), this)

    }


}