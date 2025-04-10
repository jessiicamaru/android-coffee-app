package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.coffeeshop.R
import com.example.coffeeshop.data_class.LocationData
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import java.util.Locale

class OnBoarding : Activity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private var store = Store.Companion.store;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_boarding)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager // Khởi tạo LocationManager

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                10f,
                this
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        val getStarted: Button = findViewById(R.id.getStartedButton)
        getStarted.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        getLocation()
    }

    // -------------------------------- Location ---------------------------------------- //
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        Toast.makeText(
            this,
            "Location: ${location.latitude}, ${location.longitude}",
            Toast.LENGTH_SHORT
        ).show()

        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]?.getAddressLine(0)
                store.dispatch(Action.SetAddress(address))
                store.dispatch(
                    Action.SetLocation(
                        LocationData(
                            longitude = location.longitude, latitude = location.latitude
                        )
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Xử lý trạng thái provider thay đổi (nếu cần thiết)
    }

    override fun onProviderEnabled(provider: String) {
        // Xử lý khi provider được bật
    }

    override fun onProviderDisabled(provider: String) {
        // Xử lý khi provider bị tắt
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy đăng ký cập nhật vị trí khi Activity bị hủy
        locationManager.removeUpdates(this)
    }

    // Xử lý yêu cầu quyền truy cập vị trí
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, đăng ký LocationListener
                getLocation()
            } else {
                // Quyền bị từ chối
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
