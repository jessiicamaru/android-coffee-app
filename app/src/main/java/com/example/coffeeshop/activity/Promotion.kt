package com.example.coffeeshop.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.toast

class Promotion : Activity() {
    private var store = Store.store
    private val service = Service()
    private lateinit var returnButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.promotion)

        store.dispatch(Action.AddHistory(this))

        returnButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, store.state.historyList.last()::class.java)
            startActivity(intent)
        }

        store.subscribe {
            runOnUiThread {
                updateUI(store.state)
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )
    }

    private fun updateUI(state: AppState) {

    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "Order received status for Order $orderId: $status")

            toast(this@Promotion) {
                "Your order status is changed"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
        WebSocketManager.getInstance(this).disconnect()
    }
}