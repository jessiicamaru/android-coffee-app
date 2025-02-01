package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.coffeeshop.R

class Detail : Activity() {
    private lateinit var coffeeImage: ImageView
    private lateinit var coffeeTitle: TextView
    private lateinit var categoryTitle: TextView
    private lateinit var coffeeCost: TextView
    private lateinit var coffeeDescription: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail)

        coffeeImage = findViewById(R.id.coffee_image)
        coffeeTitle = findViewById(R.id.coffee_title)
        categoryTitle = findViewById(R.id.category_title)
        coffeeCost = findViewById(R.id.coffee_cost)
        coffeeDescription = findViewById(R.id.coffee_description)

        val returnButton: ImageButton = findViewById(R.id.return_button)
        returnButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        val title = intent.getStringExtra("coffeeTitle")
        val photoUrl = intent.getStringExtra("coffeePhotoUrl")
        val cost = intent.getDoubleExtra("coffeeCost", 0.0)
        val description = intent.getStringExtra("coffeeDescription")
        val category = intent.getStringExtra("categoryTitle")

        coffeeTitle.text = title
        categoryTitle.text = category
        coffeeCost.text = "$ $cost"
        coffeeDescription.text = description

        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.caffe_mocha)
            .error(R.drawable.caffe_mocha)
            .into(coffeeImage)
    }
}