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
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.view.animation.TranslateAnimation
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.example.coffeeshop.R
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
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

    private val store = Store.store

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContentView(R.layout.map)

        val returnButton: LinearLayout = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        mapView = findViewById(R.id.map_view)
        mapView.getMapAsync(this)

        pendingStat = findViewById(R.id.pending_stat)
        preparingStat = findViewById(R.id.preparing_stat)
        deliveringStat = findViewById(R.id.delivering_stat)
        completeStat = findViewById(R.id.complete_stat)
        customerName = findViewById(R.id.customer_name)
        timeRemaining = findViewById(R.id.time_remaining)
        status = findViewById(R.id.status)

        val orderId = intent.getStringExtra("orderId")
        val statIntent = intent.getStringExtra("stat")
        val feeIntent = intent.getStringExtra("fee")

        updateUI(orderId, statIntent, feeIntent)
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
            val order = store.state.ordersPending?.find { it.orderId == orderId } ?: return
            stat = order.stat
            fee = order.fee
            customer = order.userInfo?.name
        } else {
            return
        }

        val distance = fee?.div(0.3)
        val time = distance?.times(8)?.toInt()

        customerName.text = customer ?: "Unknown"
        val stats = listOf(pendingStat, preparingStat, deliveringStat, completeStat)
        for (i in 0..stat.coerceAtMost(stats.size - 1)) {
            if (i != stat) {
                stats[i].setBackgroundColor(Color.parseColor("#36C07E"))
            } else applyLoadingEffect(stats[i])
        }

        when (stat) {
            0 -> {
                status.text = "Pending"
                timeRemaining.text = "${15 + (time ?: 0)} minutes left"
            }

            1 -> {
                status.text = "Preparing"
                status.setTextColor(Color.parseColor("#FFA955"))
                timeRemaining.text = "${10 + (time ?: 0)} minutes left"
            }

            2 -> {
                status.text = "Delivering"
                status.setTextColor(Color.parseColor("#C67C4E"))
                timeRemaining.text = "${time ?: 0} minutes left"
            }

            3 -> {
                status.text = "Success"
                status.setTextColor(Color.parseColor("#36C07E"))
                timeRemaining.text = "Your order is arrived"
            }
        }
    }


    private fun applyLoadingEffect(view: LinearLayout) {
        // Tạo LayerDrawable: nền xám nhạt + thanh loading
        val loadingBar = ColorDrawable(Color.parseColor("#36C07E")).apply {
            setBounds(0, 0, 50, view.height) // Thanh rộng 50dp
        }
        val background = ColorDrawable(Color.TRANSPARENT) // Nền trong suốt
        val layerDrawable = LayerDrawable(arrayOf(background, loadingBar))

        // Đảm bảo view đã được vẽ trước khi áp dụng animation
        view.post {
            view.background = layerDrawable

            // Tạo TranslateAnimation để di chuyển thanh loading từ trái sang phải
            val animation = TranslateAnimation(
                -view.width.toFloat(),                      // fromXDelta: bắt đầu từ ngoài bên trái
                0f, // toXDelta: di chuyển đến bên phải
                0f,                        // fromYDelta
                0f                         // toYDelta
            ).apply {
                duration = 1500 // 1.5 giây
                repeatCount = TranslateAnimation.INFINITE // Lặp vô hạn
                repeatMode = TranslateAnimation.RESTART // Bắt đầu lại từ đầu
            }

            // Áp dụng animation cho layer thứ 1 (thanh loading)
            view.startAnimation(animation)
            animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {
                    layerDrawable.setLayerInset(1, -50, 0, 0, 0) // Đặt lại vị trí ban đầu (ngoài bên trái)
                }
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {
                    layerDrawable.setLayerInset(1, -50, 0, 0, 0) // Reset khi lặp
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
        super.onStart(); mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); mapView.onPause()
    }

    override fun onStop() {
        super.onStop(); mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy(); mapView.onDestroy()
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
}