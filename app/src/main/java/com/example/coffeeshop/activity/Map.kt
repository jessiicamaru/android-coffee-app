package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.example.coffeeshop.R
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.json.JSONObject
import org.maplibre.android.geometry.LatLngBounds
import java.io.IOException

class Map : AppCompatActivity(), OnMapReadyCallback {
    // Declare a variable for MapView
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap

    private val store = Store.store

    //20 Loc Ninh: 15.990506012096326, 108.2561075672051

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init MapLibre
        MapLibre.getInstance(this)

        setContentView(R.layout.map)

        val returnButton: LinearLayout = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
        // Init the MapView
        mapView = findViewById(R.id.map_view)
        mapView.getMapAsync(this)
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

    private fun drawRouteFromAPI() {
        val url =
            "https://router.project-osrm.org/route/v1/driving/108.2561075672051,15.990506012096326;${store.state.location?.longitude},${store.state.location?.latitude}?geometries=geojson"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Route", "Lỗi khi tải tuyến đường: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val jsonObject = JSONObject(responseString)
                    val routes = jsonObject.getJSONArray("routes")
                    val geometry = routes.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                    val geoJson = """
                                {
                                  "type": "FeatureCollection",
                                  "features": [
                                    {
                                      "type": "Feature",
                                      "geometry": {
                                        "type": "LineString",
                                        "coordinates": $geometry
                                      },
                                      "properties": {}
                                    }
                                  ]
                                }
                                """.trimIndent()

                    runOnUiThread {
                        val style = mapLibreMap.style ?: return@runOnUiThread

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

                        adjustCameraToRoute(geometry)
                    }
                }
            }
        })
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