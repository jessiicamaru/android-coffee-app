package com.example.coffeeshop.toast

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.coffeeshop.R


@SuppressLint("InflateParams")
inline fun Context.toast(context: Context, message:()->String){

    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.toast_layout, null)

    val text: TextView = layout.findViewById(R.id.toast_text)
    text.text = message();

    val image: ImageView = layout.findViewById(R.id.toast_image)
    image.setImageResource(R.drawable.coffee_shop_ic);

    with(Toast(context)) {
        duration = Toast.LENGTH_SHORT
        view = layout
        show()
    }

//    Toast.makeText(this, message() , Toast.LENGTH_LONG).show()
}
