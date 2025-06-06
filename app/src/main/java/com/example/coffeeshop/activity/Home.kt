package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CategoryItemAdapter
import com.example.coffeeshop.adapter.CoffeeItemAdapter
import com.example.coffeeshop.decoration.ItemMarginRight
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.toast
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : Activity() {

    private lateinit var locationText: TextView
    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var notificationAmount: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private var store = Store.Companion.store;
    private var service = Service();

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        store.dispatch(Action.AddHistory(this))

        locationText = findViewById(R.id.location)
        notificationAmount = findViewById(R.id.notification_amount);

        coffeeRecyclerView = findViewById(R.id.recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        coffeeRecyclerView.setHasFixedSize(true)

        categoryRecyclerView = findViewById(R.id.category_recycler_view)
        categoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        categoryRecyclerView.addItemDecoration(ItemMarginRight(spacing))

        store.subscribe {
            val layoutManager = categoryRecyclerView.layoutManager
            val scrollPosition =
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            updateUI(store.state)
            layoutManager.scrollToPosition(scrollPosition)
        }

        searchInput = findViewById(R.id.inputCoffee)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUI(store.state, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        service.getAllCoffees()
        service.getAllCategories()
        store.state.user?.let {
            service.getLikeCoffees(it.uid)
            WebSocketManager.connect(this, it.uid)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> return@setOnItemSelectedListener true
                R.id.nav_cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
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


    private fun updateUI(state: AppState, searchQuery: String = "") {
        // Cập nhật UI dựa trên state hiện tại
        coffeeRecyclerView.adapter = CoffeeItemAdapter(state.coffees, this)
        categoryRecyclerView.adapter = CategoryItemAdapter(state.categories)

        Log.d("NotAmount", "${state.notifications.size}")

        if (state.notifications.size == 0) {
            notificationAmount.visibility = View.INVISIBLE
        } else {
            notificationAmount.visibility = View.VISIBLE
            notificationAmount.text = "${state.notifications.size}";
        }


        locationText.text = store.state.address

        val filteredByCategory = if (state.selectedCategory == "all") {
            state.coffees
        } else {
            state.coffees.filter { it.categoryId == state.selectedCategory }
        }

        val filteredCoffees = if (searchQuery.isNotBlank()) {
            filteredByCategory.filter { it.coffeeTitle.contains(searchQuery, ignoreCase = true) }
        } else {
            filteredByCategory
        }

        coffeeRecyclerView.adapter = CoffeeItemAdapter(ArrayList(filteredCoffees), this)
    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "MainActivity received status for Order $orderId: $status")
            // TODO: Cập nhật UI hoặc chuyển màn hình

            toast(this@Home) {
                "Your order status is changed"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
//        WebSocketManager.disconnect()
    }

    override fun onResume() {
        super.onResume()
        store.dispatch(Action.RefreshOrders)
        bottomNavigationView.selectedItemId = R.id.nav_home
    }
}
