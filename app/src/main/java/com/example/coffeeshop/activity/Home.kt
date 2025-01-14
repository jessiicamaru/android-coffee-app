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
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.coffeeshop.R
import com.example.coffeeshop.api_interface.CategoryApi
import com.example.coffeeshop.api_interface.CoffeeApi
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class Home : Activity(), LocationListener {
    private var BASE_URL = "http://10.0.2.2:5000/"
    private var TAG = "DATA_RESPONSE"
    private lateinit var locationManager: LocationManager
    private lateinit var locationText: TextView

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        locationText = findViewById(R.id.location) // Lấy view TextView để hiển thị địa chỉ

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager // Khởi tạo LocationManager

        // Kiểm tra và yêu cầu quyền truy cập vị trí
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Đăng ký LocationListener để nhận cập nhật vị trí
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L, // Interval thời gian (ms)
                10f,   // Interval khoảng cách (m)
                this   // LocationListener
            )
        } else {
            // Nếu chưa cấp quyền, yêu cầu quyền vị trí tại runtime
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // Xử lý sự kiện click vào các layout con
        val layoutContainer = findViewById<GridLayout>(R.id.items_layout)

        // Duyệt qua tất cả các con của GridLayout (tất cả ConstraintLayouts)
        for (i in 0 until layoutContainer.childCount) {
            val layout = layoutContainer.getChildAt(i) as? ConstraintLayout
            layout?.setOnClickListener { view ->
                val intent = Intent(this, Detail::class.java)
                startActivity(intent)
            }
        }

        getLocation()
        getAllCoffees()
        getAllCategories()
    }

    private fun getAllCoffees() {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CoffeeApi::class.java)

        api.getAllCoffees().enqueue(object : Callback<List<Coffee>> {
            override fun onResponse(call: Call<List<Coffee>>, response: Response<List<Coffee>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        for (i in it) {
                            Log.i(TAG, "On Res: ${i.coffeeId}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Coffee>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }

        })
    }

    private fun getAllCategories() {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CategoryApi::class.java)

        api.getAllCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        for (i in it) {
                            Log.i(TAG, "On Res: ${i.categoryId}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Log.i(TAG, "On Fail: ${t.message}")
            }


        })
    }

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
        // Hiển thị tọa độ khi vị trí thay đổi
        Toast.makeText(
            this,
            "Location: ${location.latitude}, ${location.longitude}",
            Toast.LENGTH_SHORT
        ).show()

        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            // Nếu có kết quả, hiển thị địa chỉ
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]?.getAddressLine(0)
                locationText.text = address // Hiển thị địa chỉ vào TextView
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
