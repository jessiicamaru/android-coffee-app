package com.example.coffeeshop.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.example.coffeeshop.R

class Detail : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail)
    }
}