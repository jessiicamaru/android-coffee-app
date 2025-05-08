package com.example.coffeeshop.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private const val BASE_URL = "ws://10.0.2.2:5000/order-status"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS) // Gửi ping mỗi 30 giây để giữ kết nối
        .build()
    private var isConnected = false
    private var reconnectAttempts = 0
    private const val MAX_RECONNECT_ATTEMPTS = 5

    fun connect(context: Context, uid: String, onConnected: () -> Unit = {}) {
        if (isConnected) {
            onConnected.invoke()
            return
        }

        val request = Request.Builder()
            .url("$BASE_URL?uid=$uid")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                reconnectAttempts = 0
                Log.d("WebSocket", "Connected successfully with UID: $uid")
                onConnected.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received data: $text")
                broadcastMessage(context, text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $reason")
                isConnected = false
                webSocket.close(1000, "Client disconnect")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Failure: ${t.message}")
                isConnected = false
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++
                    Log.d("WebSocket", "Reconnecting... Attempt $reconnectAttempts")
                    connect(context, uid, onConnected)
                }
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
        Log.d("WebSocket", "Disconnected")
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    private fun broadcastMessage(context: Context, message: String) {
        val intent = Intent(ACTION_ORDER_STATUS)
        intent.putExtra(EXTRA_ORDER_ID, parseOrderId(message))
        intent.putExtra(EXTRA_STATUS, parseStatus(message))
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun parseOrderId(message: String): String? {
        return try {
            val json = JSONObject(message)
            json.getString("orderId")
        } catch (e: Exception) {
            Log.e("WebSocket", "Failed to parse orderId: ${e.message}")
            null
        }
    }

    private fun parseStatus(message: String): Int {
        return try {
            val json = JSONObject(message)
            json.getInt("status")
        } catch (e: Exception) {
            Log.e("WebSocket", "Failed to parse status: ${e.message}")
            -1
        }
    }

    const val ACTION_ORDER_STATUS = "com.example.coffeeshop.ACTION_ORDER_STATUS"
    const val EXTRA_ORDER_ID = "orderId"
    const val EXTRA_STATUS = "status"
}