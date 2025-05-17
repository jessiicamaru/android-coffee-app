package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CoffeeItemOrderAdapter
import com.example.coffeeshop.constants.PromotionType
import com.example.coffeeshop.data_class.CoffeeRequest
import com.example.coffeeshop.data_class.OrderRequest
import com.example.coffeeshop.data_class.FilterPromotionRequest
import com.example.coffeeshop.data_class.LocationData
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.calculatePromotions
import com.example.coffeeshop.utils.toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.util.UUID
import java.util.concurrent.TimeUnit

class OnOrder : Activity() {

    private lateinit var coffeeRecyclerView: RecyclerView
    private lateinit var proTotal: TextView
    private lateinit var ogTotal: TextView
    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var orderButton: Button
    private lateinit var ogFee: TextView
    private lateinit var proFee: TextView
    private lateinit var returnButton: ImageButton
    private lateinit var openPromotion: GridLayout
    private lateinit var discountTV: TextView
    private lateinit var editName: LinearLayout
    private lateinit var editAddress: LinearLayout
    private lateinit var addNote: LinearLayout
    private lateinit var note: TextView
    private lateinit var noteContainer: LinearLayout
    private val service = Service()
    private val store = Store.store
    private var shippingFee: Double = 1.0
    private var price: Double = 0.0
    private var distanceKm: Double? = null // Lưu khoảng cách
    private var geometry: JSONArray? = null // Lưu dữ liệu geometry từ getDistance

    companion object {
        private const val REQUEST_CODE_ADDRESS = 1001
    }

    @SuppressLint("SetTextI18n", "DefaultLocale", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_order)

        store.dispatch(Action.AddHistory(this))

        coffeeRecyclerView = findViewById(R.id.order_recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 1)
        coffeeRecyclerView.setHasFixedSize(true)

        returnButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            runOnUiThread {
                store.dispatch(Action.RemoveOrderInfo)
                store.dispatch(Action.RemoveTempLocation)
            }
            store.dispatch(Action.RemoveHistory)
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        noteContainer = findViewById(R.id.note_container)
        note = findViewById(R.id.note)
        addNote = findViewById(R.id.add_note)
        editName = findViewById(R.id.edit_name)
        editAddress = findViewById(R.id.edit_address)
        openPromotion = findViewById(R.id.open_promotion)
        proTotal = findViewById(R.id.pro_total)
        ogTotal = findViewById(R.id.og_total)
        name = findViewById(R.id.name)
        address = findViewById(R.id.adress)
        orderButton = findViewById(R.id.order)
        ogFee = findViewById(R.id.og_fee)
        proFee = findViewById(R.id.pro_fee)
        discountTV = findViewById(R.id.discount_tv)

        orderButton.isClickable = false
        orderButton.isEnabled = false
        orderButton.setBackgroundResource(R.drawable.button_non_primary)
        orderButton.setTextColor(Color.BLACK)

        store.state.user?.let { user ->
            WebSocketManager.connect(this, user.uid) {
            }
        }

        if (store.state.shippingFee != null && store.state.distanceKm != null) {
            shippingFee = store.state.shippingFee!!
            distanceKm = store.state.distanceKm!!
            geometry = store.state.mapData
            Log.d("Route", "1")
            applyPromotions()
        } else if (savedInstanceState != null) {
            Log.d("Route", "2")
            shippingFee = savedInstanceState.getDouble("shippingFee", 1.0)
            price = savedInstanceState.getDouble("price", 0.0)
            distanceKm = savedInstanceState.getDouble("distanceKm", -1.0).takeIf { it != -1.0 }
            val geometryString = savedInstanceState.getString("geometry")
            if (geometryString != null) {
                geometry = JSONArray(geometryString)
                store.dispatch(Action.SetMapData(geometry!!))
            }
            applyPromotions()
        } else {
            getDistance()
        }

        if (store.state.note != null && store.state.note!!.isNotEmpty()) {
            noteContainer.visibility = View.VISIBLE
            note.text = store.state.note
        } else {
            noteContainer.visibility = View.GONE
        }

        // Khởi tạo name từ AppState
        name.text = store.state.receiveCustomer ?: store.state.user?.displayName ?: ""

        addNote.setOnClickListener {
            showAddNote()
        }

        editName.setOnClickListener {
            showEditNameDialog()
        }

        editAddress.setOnClickListener {
            val intent = Intent(this, ChangeLocation::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADDRESS)
        }

        openPromotion.setOnClickListener {
            Log.d("OpenPromotion", "1")
            service.filterPromotion(FilterPromotionRequest(
                uid = store.state.user?.uid ?: "",
                totalAmount = price,
                distance = shippingFee / 0.3,
                itemCount = store.state.orders.sumOf { coffee -> coffee.quantity }
            )) {
                val intent = Intent(this, Promotion::class.java)
                startActivity(intent)
            }
        }

        orderButton.setOnClickListener {
            Log.d("OnOrder", "Order button clicked")
            if (store.state.user == null) {
                Log.e("OnOrder", "User is null, cannot create order")
                runOnUiThread {
                    toast(this) { "User not logged in" }
                }
                return@setOnClickListener
            }

            val user = store.state.user!!
            val coffeesToOrder = store.state.orders.map { coffee ->
                CoffeeRequest(
                    coffeeId = coffee.coffeeId,
                    size = coffee.size,
                    quantity = coffee.quantity
                )
            }
            val orderId = UUID.randomUUID().toString()

            price = store.state.orders.sumOf { it.coffeeCost * it.quantity }
            Log.d("ORDER", "Price before sending: $price")

            if (store.state.address == null) {
                Log.e("OnOrder", "Address is null, cannot create order")
                runOnUiThread {
                    toast(this@OnOrder) { "Address not set" }
                }
                return@setOnClickListener
            }

            val orderAddress = store.state.address!!

            val proTotalValue = proTotal.text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            val proFeeValue = proFee.text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            val ogTotalValue =
                if (ogTotal.visibility == View.VISIBLE) ogTotal.text.toString().replace("$", "")
                    .toDoubleOrNull() ?: proTotalValue else proTotalValue
            val ogFeeValue =
                if (ogFee.visibility == View.VISIBLE) ogFee.text.toString().replace("$", "")
                    .toDoubleOrNull() ?: proFeeValue else proFeeValue
            val receiveCustomer = name.text.toString()
            val note = note.text.toString()
            val orderRequest = OrderRequest(
                uid = user.uid,
                coffees = coffeesToOrder,
                orderId = orderId,
                address = store.state.tempAddress ?: orderAddress,
                total = proTotalValue,
                fee = proFeeValue,
                longitude = store.state.tempLocation?.longitude ?: store.state.location?.longitude ?: 0.0,
                latitude = store.state.tempLocation?.latitude ?: store.state.location?.latitude ?: 0.0,
                note = note,
                originalTotal = ogTotalValue,
                originalFee = ogFeeValue,
                receiveCustomer = receiveCustomer,
                promotion = store.state.selectedPromotions
            )

            runOnUiThread {
                service.createOrder(orderRequest) { success ->
                    if (success) {
                        store.dispatch(Action.RemoveCart)
                        store.dispatch(Action.RemoveOrderInfo)
                        WebSocketManager.sendMessage("Order created: $orderId") // Sử dụng trực tiếp WebSocketManager

                        val intent = Intent(this, Map::class.java).apply {
                            putExtra("stat", 0)
                            putExtra("fee", shippingFee.toString())
                            putExtra("orderId", orderId)
                            putExtra("source", "OnOrder")
                            putExtra("receiveCustomer", receiveCustomer)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("OnOrder", "Failed to create order: $orderId")
                        runOnUiThread {
                            toast(this@OnOrder) { "Failed to create order" }
                        }
                    }
                }
            }
        }

        val radioGroup = findViewById<RadioGroup>(R.id.delivery_group)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                for (i in 0 until group.childCount) {
                    val radioButton = group.getChildAt(i) as? RadioButton
                    radioButton?.let {
                        it.setBackgroundResource(R.drawable.radio_button)
                        if (it.id == checkedId) {
                            it.setTextColor(Color.WHITE)
                        } else {
                            it.setTextColor(Color.BLACK)
                        }
                    }
                }
            }
        }

        store.subscribe {
            runOnUiThread {
                updateUI(store.state)
            }
        }

        store.dispatch(Action.RefreshOrders)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateUI(state: AppState) {
        Log.d("UPDATE_UI_NOW", "Orders: ${state.orders}")
        discountTV.text = "${state.selectedPromotions.size} Discount is Applied"
        price = state.orders.sumOf { it.coffeeCost * it.quantity }
        applyPromotions()

        name.text = state.receiveCustomer ?: state.user?.displayName ?: ""
        address.text = state.tempAddress ?: state.address ?: ""

        if (!state.note.isNullOrEmpty()) {
            noteContainer.visibility = View.VISIBLE
            note.text = state.note
        } else {
            noteContainer.visibility = View.GONE
        }
        coffeeRecyclerView.adapter = CoffeeItemOrderAdapter(ArrayList(state.orders), this)
    }

    @SuppressLint("SetTextI18n")
    private fun applyPromotions() {
        val df = DecimalFormat("#.##")

        val originalFee = if (distanceKm != null) distanceKm!! * 0.3 * 1.3 else 0.0
        val originalTotal = price

        val (discountedPrice, discountedFee) = calculatePromotions(
            price = price,
            distanceKm = distanceKm,
            promotions = store.state.promotions
        )


        // Lấy danh sách PromotionResponse từ store.state.promotions dựa trên selectedPromotions
        val selectedPromotionResponses = store.state.selectedPromotions.mapNotNull { selected ->
            store.state.promotions.find { it.promotionId == selected.promotionId }
        }

        // Kiểm tra xem có promotion nào thuộc loại "product" hoặc "shipping" không
        val hasProductPromotion =
            selectedPromotionResponses.any { it.promotionType.name == PromotionType.product.name }
        val hasShippingPromotion =
            selectedPromotionResponses.any { it.promotionType.name == PromotionType.shipping.name }

        runOnUiThread {
            // Xử lý ogTotal và proTotal (giá sản phẩm)
            if (hasProductPromotion) {
                ogTotal.visibility = View.VISIBLE
                ogTotal.text = "${df.format(originalTotal)}$"
                proTotal.text = "${df.format(discountedPrice)}$"
            } else {
                ogTotal.visibility = View.GONE
                proTotal.text = "${df.format(price)}$"
            }

            // Xử lý ogFee và proFee (phí vận chuyển)
            if (hasShippingPromotion) {
                ogFee.visibility = View.VISIBLE
                ogFee.text = "${df.format(originalFee)}$"
                proFee.text = "${df.format(discountedFee)}$"
            } else {
                ogFee.visibility = View.GONE
                proFee.text = "${df.format(originalFee)}$"
            }

            // Cập nhật trạng thái nút order
            orderButton.isClickable = true
            orderButton.isEnabled = true
            orderButton.setBackgroundResource(R.drawable.button_primary)
            orderButton.setTextColor(Color.WHITE)
        }
    }

    private fun getDistance(retryCount: Int = 0, maxRetries: Int = 3) {
        Log.d("LOCATION", "${
            store.state.tempLocation?.longitude
        } ${store.state.tempLocation?.latitude}")

        val url =
            "https://router.project-osrm.org/route/v1/driving/108.2561075672051,15.990506012096326;${store.state.tempLocation?.longitude ?: store.state.location?.longitude},${store.state.tempLocation?.latitude ?:store.state.location?.latitude}?geometries=geojson"

        Log.d("Route", url)

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Route", "Lỗi khi tải tuyến đường: ${e.message}")
                if (retryCount < maxRetries) {
                    Log.d("Route", "Thử lại lần ${retryCount + 1}/$maxRetries")
                    getDistance(retryCount + 1, maxRetries)
                } else {
                    runOnUiThread {
                        ogFee.text = "Error"
                        proFee.text = "Error"
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                Log.d("Mapshit", "1")
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val jsonObject = JSONObject(responseString)
                    val routes = jsonObject.getJSONArray("routes")
                    val route = routes.getJSONObject(0)

                    distanceKm = route.getDouble("distance") / 1000.0
                    shippingFee = distanceKm!! * 0.3

                    val df = DecimalFormat("#.##")
                    val formattedDistance = df.format(distanceKm)
                    val formattedFee = df.format(shippingFee)

                    Log.d("SHIPPING", "Distance: $formattedDistance km, Fee: $$formattedFee")

                    geometry = route.getJSONObject("geometry").getJSONArray("coordinates")

                    store.dispatch(Action.SetShippingData(shippingFee, distanceKm!!))
                    store.dispatch(Action.SetMapData(geometry!!))

                    applyPromotions()
                }
            }
        })
    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "OnOrder received status for Order $orderId: $status")
            runOnUiThread {
                toast(this@OnOrder) {
                    "Your order status is changed"
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("shippingFee", shippingFee)
        outState.putDouble("price", price)
        outState.putDouble("distanceKm", distanceKm ?: -1.0)
        geometry?.let { outState.putString("geometry", it.toString()) }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
    }

    private fun showAddNote() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add note")

        val input = EditText(this)
        input.setText(note.text)
        input.hint = "Add note"
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newNote = input.text.toString().trim()
            if (newNote.isNotEmpty()) {
                noteContainer.visibility = View.VISIBLE
                note.text = newNote
                store.dispatch(Action.SetNote(newNote))
            } else {
                noteContainer.visibility = View.GONE
                store.dispatch(Action.SetNote(null))
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Receiver Name")

        val input = EditText(this)
        input.setText(name.text)
        input.hint = "Enter new name"
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                name.text = newName
                store.dispatch(Action.SetReceiveCustomer(newName))
            } else {
                toast(this) { "Name cannot be empty" }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == Activity.RESULT_OK) {
            val selectedAddress = data?.getStringExtra("selected_address")
            if (selectedAddress != null) {
                address.text = selectedAddress
                Log.d("onActivityResult","1")
                getDistance()
            }
        }
    }
}