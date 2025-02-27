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

class Like : Activity() {
    private lateinit var homeButton: LinearLayout;
    private lateinit var bagButton: LinearLayout;

    private lateinit var coffeeRecyclerView: RecyclerView


    private var store = Store.Companion.store;
    private val service = Service();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.like)

        store.dispatch(Action.AddHistory(this))

        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        bagButton = findViewById(R.id.bagButton)
        bagButton.setOnClickListener {
            val intent = Intent(this, Cart::class.java)
            startActivity(intent)
        }

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

    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI", "FAV: ${state.likeCoffees}")

        val list = state.coffees.filter { coffee ->  state.likeCoffees.find { it == coffee.coffeeId } != null}

        coffeeRecyclerView.adapter = CoffeeItemLikeAdapter(ArrayList(list), this)

    }

}