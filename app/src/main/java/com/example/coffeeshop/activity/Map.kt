package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import android.animation.ValueAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.animation.TranslateAnimation
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.example.coffeeshop.R
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.toast
import org.json.JSONArray
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.geometry.LatLngBounds

class Map : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var pendingStat: LinearLayout
    private lateinit var preparingStat: LinearLayout
    private lateinit var deliveringStat: LinearLayout
    private lateinit var completeStat: LinearLayout
    private lateinit var status: TextView
    private lateinit var customerName: TextView
    private lateinit var timeRemaining: TextView
    private var currentOrderId: String? = null
    private var currentFee: Double? = null // Lưu fee để không phải tìm lại trong store
    private val store = Store.store
    private val service = Service();

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContentView(R.layout.map)

        store.dispatch(Action.AddHistory(this))

        currentOrderId = intent.getStringExtra("orderId")
        val statIntent = intent.getStringExtra("stat")
        val feeIntent = intent.getStringExtra("fee")

        val returnButton: LinearLayout = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val source = intent.getStringExtra("source")
            Log.d("source", "$source");
            if (source == "OnOrder") {
                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, store.state.historyList.last()::class.java).apply {
                    putExtra("orderId", currentOrderId)
                }
                startActivity(intent)
            }
        }

        mapView = findViewById(R.id.map_view)
        mapView.getMapAsync(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )

        pendingStat = findViewById(R.id.pending_stat)
        preparingStat = findViewById(R.id.preparing_stat)
        deliveringStat = findViewById(R.id.delivering_stat)
        completeStat = findViewById(R.id.complete_stat)
        customerName = findViewById(R.id.customer_name)
        timeRemaining = findViewById(R.id.time_remaining)
        status = findViewById(R.id.status)

        // Lưu fee vào biến thành viên nếu có
        if (feeIntent != null) {
            currentFee = feeIntent.toDouble()
        }
        store.state.user?.let { service.getPendingOrder(it.uid) }

        store.subscribe {
            runOnUiThread {
                updateUI(currentOrderId, statIntent, feeIntent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(orderId: String?, statIntent: String?, feeIntent: String?) {
        val stat: Int
        val fee: Double?
        val customer: String?

        if (statIntent != null && feeIntent != null) {
            stat = statIntent.toInt()
            fee = feeIntent.toDouble()
            customer = store.state.user?.displayName
        } else if (orderId != null) {
            val order = store.state.ordersPending.find { it.orderId == orderId }
            if (order == null) {
                Log.e("MapActivity", "Order not found in store.state.ordersPending for orderId: $orderId")
                return
            }
            stat = order.stat
            fee = order.fee // Sử dụng fee từ biến thành viên nếu không có trong store
            customer = order.userInfo.name
        } else {
            Log.e("MapActivity", "orderId is null, cannot update UI")
            return
        }

        val distance = fee.div(0.3)
        val time = distance.times(8).toInt()

        customerName.text = customer ?: "Unknown"

        // Reset trạng thái của tất cả LinearLayout trước khi áp dụng mới
        val stats = listOf(pendingStat, preparingStat, deliveringStat, completeStat)
        stats.forEach { statView ->
            statView.clearAnimation() // Xóa animation cũ
            statView.setBackgroundColor(Color.TRANSPARENT) // Reset màu nền
        }

        // Áp dụng màu và hiệu ứng loading cho các trạng thái
        for (i in 0..stat.coerceAtMost(stats.size - 1)) {
            if (i != stat) {
                stats[i].setBackgroundColor(Color.parseColor("#36C07E"))
            } else {
                applyLoadingEffect(stats[i])
            }
        }

        when (stat) {
            0 -> {
                status.text = "Pending"
                status.setTextColor(Color.parseColor("#000000")) // Màu mặc định
                timeRemaining.text = "${15 + time} minutes left"
            }
            1 -> {
                status.text = "Preparing"
                status.setTextColor(Color.parseColor("#FFA955"))
                timeRemaining.text = "${10 + time} minutes left"
            }
            2 -> {
                status.text = "Delivering"
                status.setTextColor(Color.parseColor("#C67C4E"))
                timeRemaining.text = "$time minutes left"
            }
            3 -> {
                status.text = "Success"
                status.setTextColor(Color.parseColor("#36C07E"))
                timeRemaining.text = "Your order is arrived"
            }
            4 -> {
                status.text = "Cancelled"
                status.setTextColor(Color.parseColor("#CF0F47"))
                timeRemaining.text = "Your order is cancelled"
            }
        }
    }

    private fun applyLoadingEffect(view: LinearLayout) {
        // Tạo LayerDrawable: nền trong suốt + thanh loading
        val loadingBar = ColorDrawable(Color.parseColor("#36C07E")).apply {
            setBounds(0, 0, 50, view.height) // Thanh rộng 50dp
        }
        val background = ColorDrawable(Color.TRANSPARENT)
        val layerDrawable = LayerDrawable(arrayOf(background, loadingBar))

        view.post {
            view.background = layerDrawable

            // Tạo TranslateAnimation để di chuyển thanh loading từ trái sang phải
            val animation = TranslateAnimation(
                -view.width.toFloat(), // fromXDelta: bắt đầu từ ngoài bên trái
                0f, // toXDelta: di chuyển đến bên phải
                0f, // fromYDelta
                0f  // toYDelta
            ).apply {
                duration = 1500
                repeatCount = TranslateAnimation.INFINITE
                repeatMode = TranslateAnimation.RESTART
            }

            view.startAnimation(animation)
            animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {
                    layerDrawable.setLayerInset(1, -50, 0, 0, 0)
                }
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {
                    layerDrawable.setLayerInset(1, -50, 0, 0, 0)
                }
            })
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        val key = "Vlq8AoHnyCUzBLQQ6wB5"
        val mapId = "streets-v2"
        mapLibreMap = map
        mapLibreMap.setStyle("https://api.maptiler.com/maps/$mapId/style.json?key=$key") {
            drawRouteFromAPI()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
        WebSocketManager.getInstance(this).disconnect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun drawRouteFromAPI() {
        runOnUiThread {
            val style = mapLibreMap.style ?: return@runOnUiThread
            if (store.state.mapData != null) {
                val geoJson = """
                    {
                      "type": "FeatureCollection",
                      "features": [
                        {
                          "type": "Feature",
                          "geometry": {
                            "type": "LineString",
                            "coordinates": ${store.state.mapData}
                          },
                          "properties": {}
                        }
                      ]
                    }
                """.trimIndent()
                val existingSource = style.getSourceAs<GeoJsonSource>("route-source")
                if (existingSource != null) {
                    existingSource.setGeoJson(geoJson)
                } else {
                    val source = GeoJsonSource("route-source", geoJson)
                    style.addSource(source)
                    val lineLayer = LineLayer("route-layer", "route-source").withProperties(
                        PropertyFactory.lineColor("#C67C4E"),
                        PropertyFactory.lineWidth(5f)
                    )
                    style.addLayer(lineLayer)
                }
                adjustCameraToRoute(store.state.mapData!!)
            }
        }
    }

    private fun adjustCameraToRoute(coordinates: JSONArray) {
        var minLat = Double.MAX_VALUE
        var minLng = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var maxLng = Double.MIN_VALUE
        for (i in 0 until coordinates.length()) {
            val coord = coordinates.getJSONArray(i)
            val lng = coord.getDouble(0)
            val lat = coord.getDouble(1)
            if (lat < minLat) minLat = lat
            if (lng < minLng) minLng = lng
            if (lat > maxLat) maxLat = lat
            if (lng > maxLng) maxLng = lng
        }
        val bounds = LatLngBounds.Builder()
            .include(LatLng(minLat, minLng))
            .include(LatLng(maxLat, maxLng))
            .build()
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedOrderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val newStatus = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)

            Log.d("WebSocket", "Map received status for Order $receivedOrderId: $newStatus")

            if (receivedOrderId != null && receivedOrderId == currentOrderId) {
                Log.d("WebSocket", "Updated")

                // Cập nhật trạng thái trong store.state.ordersPending
                val updatedOrders = store.state.ordersPending.map { order ->
                    if (order.orderId == receivedOrderId) {
                        order.copy(stat = newStatus)
                    } else {
                        order
                    }
                }
                store.dispatch(Action.SetOrders(ArrayList(updatedOrders)))

                // Gọi updateUI với orderId hiện tại và status mới
                updateUI(currentOrderId, newStatus.toString(), null)
            } else {
                toast(this@Map) {
                    "Your order status is changed"
                }
            }
        }
    }
}