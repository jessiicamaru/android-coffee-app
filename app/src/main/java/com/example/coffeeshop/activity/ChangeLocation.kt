package com.example.coffeeshop.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coffeeshop.R
import com.example.coffeeshop.data_class.LocationData
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.utils.toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import java.io.IOException

class ChangeLocation : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap
    private var locationComponent: LocationComponent? = null
    private var selectedMarker: Marker? = null
    private val store = Store.store
    private val TAG = "ChangeLocation"
    private lateinit var addressText: TextView
    private lateinit var houseNumberInput: EditText
    private lateinit var returnButton: LinearLayout
    private lateinit var chooseButton: Button

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo MapLibre với API key và WellKnownTileServer trước khi inflate layout
        val apiKey = "Vlq8AoHnyCUzBLQQ6wB5"
        MapLibre.getInstance(this, apiKey, WellKnownTileServer.MapTiler)

        setContentView(R.layout.change_location)

        // Khởi tạo các view
        mapView = findViewById(R.id.map_view)
        addressText = findViewById(R.id.address_text)
        houseNumberInput = findViewById(R.id.house_number_input)
        returnButton = findViewById(R.id.return_button)
        chooseButton = findViewById(R.id.choose_location)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Thiết lập nút Return
        returnButton.setOnClickListener {
            finish()
        }

        // Thiết lập nút Choose this location
        chooseButton.setOnClickListener {
            selectedMarker?.let { marker ->
                val latLng = marker.position
                val houseNumber = houseNumberInput.text.toString().trim()
                val savedAddress = if (houseNumber.isNotEmpty()) {
                    "$houseNumber, ${addressText.text.toString()}"
                } else {
                    addressText.text.toString()
                }
                // Lưu địa chỉ và tọa độ vào Store
                runOnUiThread {
                    store.dispatch(Action.SetTempAddress(savedAddress))
                    store.dispatch(Action.SetTempLocation(LocationData(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )))
                }

                Log.d("LOCATION", "${LocationData(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )}")
                val resultIntent = Intent()
                resultIntent.putExtra("selected_address", savedAddress)
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Log.w(TAG, "No location selected")
                toast(this) { "Please select a location on the map first" }
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        val key = "Vlq8AoHnyCUzBLQQ6wB5"
        val mapId = "streets-v2"

        mapLibreMap = map
        mapLibreMap.setStyle("https://api.maptiler.com/maps/$mapId/style.json?key=$key") { style ->
            enableLocationComponent(style)
            setupMapClickListener()
        }

        // Kiểm tra quyền sau khi bản đồ sẵn sàng
        checkPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (hasPermissions()) {
            locationComponent = mapLibreMap.locationComponent
            val locationComponentOptions = LocationComponentOptions.builder(this)
                .pulseEnabled(true)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(locationComponentOptions)
                .build()

            // Kích hoạt LocationComponent trước khi sử dụng
            locationComponent?.activateLocationComponent(locationComponentActivationOptions)

            // Sau khi kích hoạt, mới thiết lập các thuộc tính khác
            locationComponent?.isLocationComponentEnabled = true
            locationComponent?.cameraMode = CameraMode.TRACKING
            locationComponent?.renderMode = RenderMode.COMPASS

            // Điều chỉnh camera đến vị trí hiện tại
            adjustCameraToCurrentLocation()
        } else {
            checkPermissions()
        }
    }

    private fun adjustCameraToCurrentLocation() {
        // Lấy vị trí hiện tại từ LocationComponent
        locationComponent?.lastKnownLocation?.let { location ->
            val currentPosition = LatLng(location.latitude, location.longitude)
            // Điều chỉnh camera đến vị trí hiện tại với mức zoom 15
            mapLibreMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(currentPosition, 15.0),
                1000 // Thời gian animation: 1 giây
            )
        } ?: run {
            Log.w(TAG, "Last known location not available")
            toast(this) { "Unable to get current location" }
        }
    }

    private fun setupMapClickListener() {
        mapLibreMap.addOnMapClickListener { point ->
            // Xóa marker cũ nếu có
            selectedMarker?.remove()
            // Đặt marker mới tại vị trí người dùng nhấn
            selectedMarker = mapLibreMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title("Selected Location")
            )

            getAddressFromCoordinates(point.latitude, point.longitude) { address ->
                if (address != null) {
                    runOnUiThread { addressText.text = address }
                } else {
                    runOnUiThread { addressText.text = "Address: Not found" }
                }
            }
            true // Trả về true để báo rằng sự kiện đã được xử lý
        }
    }

    private fun getAddressFromCoordinates(lat: Double, lon: Double, callback: (String?) -> Unit) {
        val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "CoffeeShopApp/1.0") // Nominatim yêu cầu User-Agent
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get address: ${e.message}")
                runOnUiThread { callback(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    try {
                        val json = JSONObject(responseBody.string())
                        val displayName = json.optString("display_name", null)
                        runOnUiThread { callback(displayName) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing address: ${e.message}")
                        runOnUiThread { callback(null) }
                    }
                } ?: runOnUiThread { callback(null) }
            }
        })
    }

    private fun checkPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            mapLibreMap.getStyle { style -> enableLocationComponent(style) }
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (hasPermissions()) {
                mapLibreMap.getStyle { style -> enableLocationComponent(style) }
            } else {
                Log.e(TAG, "Location permissions denied")
                toast(this) { "Location permissions are required" }
                finish()
            }
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}