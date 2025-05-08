package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.Likes
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service
import com.example.coffeeshop.service.WebSocketManager
import com.example.coffeeshop.utils.toast

class Detail : Activity() {
    private lateinit var coffeeImage: ImageView
    private lateinit var coffeeTitle: TextView
    private lateinit var categoryTitle: TextView
    private lateinit var coffeeCost: TextView
    private lateinit var coffeeDescription: TextView
    private lateinit var likeButton: ImageView
    private lateinit var buyNowButton: Button
    private lateinit var selectedSize: String
    private val service = Service();
    private val store = Store.store

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail)

        store.dispatch(Action.AddHistory(this))

        coffeeImage = findViewById(R.id.coffee_image)
        coffeeTitle = findViewById(R.id.coffee_title)
        categoryTitle = findViewById(R.id.category_title)
        coffeeCost = findViewById(R.id.coffee_cost)
        coffeeDescription = findViewById(R.id.coffee_description)
        likeButton = findViewById(R.id.like_button)
        buyNowButton = findViewById(R.id.buyNow)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderStatusReceiver,
            IntentFilter(WebSocketManager.ACTION_ORDER_STATUS)
        )

        val returnButton: ImageButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            val orderId = intent.getStringExtra("orderId")

            if (orderId != null) {
                store.dispatch(Action.RemoveHistory)
                val intent = Intent(this, OrderDetail::class.java).apply {
                    putExtra("orderId", orderId)
                }
                startActivity(intent)
            } else {
                store.dispatch(Action.RemoveHistory)
                val intent = Intent(this, store.state.historyList.last()::class.java)
                startActivity(intent)
            }
        }

        val title = intent.getStringExtra("coffeeTitle")
        val photoUrl = intent.getStringExtra("coffeePhotoUrl")
        val cost = intent.getDoubleExtra("coffeeCost", 0.0)
        val description = intent.getStringExtra("coffeeDescription")
        val category = intent.getStringExtra("categoryTitle")
        val id = intent.getStringExtra("coffeeId")
        val categoryId = intent.getStringExtra("categoryId")

        coffeeTitle.text = title
        categoryTitle.text = category
        coffeeCost.text = "$ $cost"
        coffeeDescription.text = description

        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.caffe_mocha)
            .error(R.drawable.caffe_mocha)
            .into(coffeeImage)


        if (store.state.likeCoffees.find { it == id } != null) {
            likeButton.setImageResource(R.drawable.ic_heart_active)
        } else {
            likeButton.setImageResource(R.drawable.ic_heart)
        }

        likeButton.setOnClickListener {
            store.state.user?.let { user ->
                val like = Likes(
                    uid = user.uid,
                    coffeeId = intent.getStringExtra("coffeeId") ?: ""
                )

                Log.d("ADD_LIKE_COFFEE", "${intent.getStringExtra("coffeeId")} + ${user.uid}")


                val templist = store.state.likeCoffees;
                if (id != null) {
                    if (templist.find { it == id } != null) {
                        toast(this@Detail) {
                            "Remove $title successfully"
                        }
                        templist.remove(id)
                        likeButton.setImageResource(R.drawable.ic_heart)
                        service.deleteLikeCoffee(like)
                    } else {
                        toast(this@Detail) {
                            "Added $title successfully into favourite list"
                        }
                        templist.add(id)
                        likeButton.setImageResource(R.drawable.ic_heart_active)
                        service.addLikeCoffee(like)
                    }
                }

                store.dispatch(Action.SetLikeCoffees(templist))
            }
        }

        var displayCost = cost;

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
                val selectedValue = selectedRadioButton.text.toString()
                selectedSize = selectedValue
                when (selectedValue) {
                    "S" -> {
                        displayCost = String.format("%.2f", cost * 0.8).toDouble()
                        coffeeCost.text = "$ $displayCost"
                    }

                    "L" -> {
                        displayCost = String.format("%.2f", cost * 1.3).toDouble()
                        coffeeCost.text = "$ $displayCost"
                    }

                    "M" -> {
                        displayCost = cost
                        coffeeCost.text = "$ $displayCost"
                    }
                }
            }
        }

        buyNowButton.setOnClickListener {
            store.dispatch(
                Action.AddOrder(
                    Coffee(
                        coffeeId = id ?: "",
                        coffeeTitle = title ?: "",
                        coffeePhotoUrl = photoUrl ?: "",
                        coffeeCost = displayCost,
                        coffeeDescription = description ?: "",
                        categoryTitle = category ?: "",
                        categoryId = categoryId ?: "",
                    ),
                    size = selectedSize
                )
            )

            toast(this@Detail) {
                "Added $title successfully into cart"
            }
        }
    }

    private val orderStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val orderId = intent.getStringExtra(WebSocketManager.EXTRA_ORDER_ID)
            val status = intent.getIntExtra(WebSocketManager.EXTRA_STATUS, -1)
            Log.d("WebSocket", "Detail received status for Order $orderId: $status")
            // TODO: Cập nhật UI hoặc chuyển màn hình

            toast(this@Detail) {
                "Your order status is changed"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver)
//        WebSocketManager.disconnect()
    }
}