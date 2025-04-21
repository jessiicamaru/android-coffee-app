package com.example.coffeeshop.service

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.coffeeshop.data_class.SocketResponse
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager private constructor() {

    companion object {
        const val ACTION_ORDER_STATUS = "com.example.coffeeshop.ORDER_STATUS"
        const val EXTRA_ORDER_ID = "orderId"
        const val EXTRA_STATUS = "status"

        @Volatile
        private var INSTANCE: WebSocketManager? = null

        fun getInstance(context: Context): WebSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketManager().apply {
                    this.applicationContext = context.applicationContext
                    INSTANCE = this
                }
            }
        }
    }

    private lateinit var applicationContext: Context
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var uid: String? = null
    private var onConnected: (() -> Unit)? = null
    private var store = Store.Companion.store;

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val handler = Handler(Looper.getMainLooper())

    fun initialize(uid: String) {
        if (this.uid != uid) {
            this.uid = uid
            disconnect()
            connectWebSocket()
        }
    }

    private fun connectWebSocket() {
        if (uid == null) {
            Log.e("WebSocket", "Cannot connect - UID is null")
            return
        }

        val request = Request.Builder()
            .url("ws://10.0.2.2:5000/order-status?uid=$uid")
            .build()

        Log.d("WebSocket", "Attempting to connect to: ws://10.0.2.2:5000/order-status?uid=$uid")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("WebSocket", "Connected successfully with UID: $uid")
                onConnected?.invoke()
                onConnected = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WebSocket", "Connection failed: ${t.message}")
                handler.postDelayed({
                    if (!isConnected && uid != null) connectWebSocket()
                }, 2000)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received data: $text")
                try {
                    val jsonObject = JSONObject(text)
                    val orderId = jsonObject.getString("orderId")
                    val status = jsonObject.getInt("status")

                    // Gửi broadcast tới các activity
                    val intent = Intent(ACTION_ORDER_STATUS).apply {
                        putExtra(EXTRA_ORDER_ID, orderId)
                        putExtra(EXTRA_STATUS, status)
                    }
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                } catch (e: JSONException) {
                    Log.e("WebSocket", "JSON parsing error: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("WebSocket", "Closing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("WebSocket", "Closed: $reason")
            }
        })
    }

    fun connectAndThen(action: () -> Unit) {
        if (isConnected) {
            Log.d("WebSocket", "Already connected, executing action immediately")
            action()
        } else {
            Log.d("WebSocket", "Not connected, setting callback and initiating connection")
            onConnected = action
            connectWebSocket()
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        isConnected = false
        handler.removeCallbacksAndMessages(null)
    }
}