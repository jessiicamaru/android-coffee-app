package com.example.coffeeshop.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.coffeeshop.R

class OnBoarding : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_boarding)

        val getStarted: Button = findViewById(R.id.getStartedButton)
        getStarted.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}
