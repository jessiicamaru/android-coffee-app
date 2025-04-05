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
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service

class OrderDetail : Activity() {
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var status: TextView
    private lateinit var totalAmount: TextView
    private lateinit var orderIdTV: TextView
    private lateinit var openMap: Button
    private lateinit var coffeeRecyclerView: RecyclerView
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



        openMap.setOnClickListener {

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

        val total = coffees.sumOf { it.coffeeCost * it.quantity }
        totalAmount.text = "$total"


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
}