package com.example.coffeeshop.service

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

    fun connectWebSocket() {
        val request = Request.Builder().url("ws://10.0.2.2:5000/order-status?uid=$uid").build()
        val client = OkHttpClient()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                onConnected?.invoke()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WebSocket", "Lỗi kết nối: ${t.message}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Nhận dữ liệu: $text")

                // Parse JSON từ Server gửi về (giả sử server gửi dưới dạng JSON)
                try {
                    val jsonObject = JSONObject(text)
                    val orderId = jsonObject.getString("orderId")
                    val status = jsonObject.getInt("status")

                    onOrderStatusReceived?.invoke(orderId, status)

                } catch (e: JSONException) {
                    Log.e("WebSocket", "Lỗi phân tích JSON: ${e.message}")
                }
            }
        })
    }

    fun connectAndThen(action: () -> Unit) {
        if (isConnected) {
            action()
        } else {
            onConnected = action
            connectWebSocket()
        }
    }

    fun setOnOrderStatusReceivedListener(listener: (String, Int) -> Unit) {
        onOrderStatusReceived = listener
    }


}
