package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemCartAdapter
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.Likes
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.toast.toast

class OnOrder : Activity() {

    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var totalAmount: TextView

    private val service = Service();
    private val store = Store.store

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

        store.subscribe {
            updateUI(store.state)
        }

        store.dispatch(Action.RefreshOrders)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI_NOW", "Orders: ${state.orders}")

        val totalMoney = state.orders.sumOf { it.coffeeCost * it.quantity }

        totalAmount.text = "Total: ${String.format("%.2f", totalMoney).toDouble()}$"

        coffeeRecyclerView.adapter = CoffeeItemOrderAdapter(ArrayList(state.orders), this)
    }
}