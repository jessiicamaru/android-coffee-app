package com.example.coffeeshop.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager(private val uid: String) {

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var onConnected: (() -> Unit)? = null
    private var onOrderStatusReceived: ((String, Int) -> Unit)? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val handler = Handler(Looper.getMainLooper())

    fun connectWebSocket() {
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
                // Thử kết nối lại sau 2 giây nếu thất bại
                handler.postDelayed({
                    if (!isConnected) connectWebSocket()
                }, 2000) // 2000ms = 2 giây
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received data: $text")
                try {
                    val jsonObject = JSONObject(text)
                    val orderId = jsonObject.getString("orderId")
                    val status = jsonObject.getInt("status")
                    onOrderStatusReceived?.invoke(orderId, status)
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

    fun setOnOrderStatusReceivedListener(listener: (String, Int) -> Unit) {
        onOrderStatusReceived = listener
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        isConnected = false
        handler.removeCallbacksAndMessages(null) // Xóa các tác vụ đã lên lịch
    }
}