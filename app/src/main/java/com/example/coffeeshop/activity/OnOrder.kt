package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.CoffeeRequest
import com.example.coffeeshop.data_class.OrderRequest
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import java.util.UUID

class OnOrder : Activity() {

    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var orderButton: Button


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
        name = findViewById(R.id.name)
        address = findViewById(R.id.adress)
        orderButton = findViewById(R.id.order)
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

                val webSocketManager = WebSocketManager(user.uid)

                webSocketManager.connectAndThen {
                    service.createOrder(
                        OrderRequest(
                            uid = user.uid,
                            coffees = coffeesToOrder,
                            orderId = orderId
                        )
                    )

                    Log.d("ORDER_ID", orderId)

                    val intent = Intent(this, Map::class.java)
                    startActivity(intent)
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
                            it.setTextColor(android.graphics.Color.WHITE)
                        } else {
                            it.setTextColor(android.graphics.Color.BLACK)
                        }
                    }
                }
            }
        }


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
        name.text =
            if (state.user?.displayName != null) state.user.displayName else "Jl. Kpg Sutoyo";
        address.text = state.address ?: "Kpg. Sutoyo No. 620, Bilzen, Tanjungbalai."

        coffeeRecyclerView.adapter = CoffeeItemOrderAdapter(ArrayList(state.orders), this)
    }
}