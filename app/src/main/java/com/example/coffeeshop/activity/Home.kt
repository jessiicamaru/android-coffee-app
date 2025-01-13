package com.example.coffeeshop.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import com.example.coffeeshop.R

class Home: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("---------------------", "----------------------------")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)


        val layoutContainer = findViewById<GridLayout>(R.id.items_layout)

        // Duyệt qua tất cả các con của LinearLayout (tất cả ConstraintLayouts)
        for (i in 0 until layoutContainer.childCount) {
            val layout = layoutContainer.getChildAt(i) as? androidx.constraintlayout.widget.ConstraintLayout
            layout?.setOnClickListener { view ->
                val intent = Intent(this, Detail::class.java)
                startActivity(intent)
            }
        }

    }
}