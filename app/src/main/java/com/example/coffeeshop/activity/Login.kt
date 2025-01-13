package com.example.coffeeshop.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.coffeeshop.R
import android.widget.LinearLayout
import android.util.Log

class Login: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val myLinearLayout = findViewById<LinearLayout>(R.id.gmail_button)
        myLinearLayout.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }
}
