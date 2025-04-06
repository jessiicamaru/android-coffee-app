package com.example.coffeeshop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.activity.Detail
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service

class CoffeeItemOrderPendingAdapter(
    private val coffeeList: ArrayList<Coffee>,
    private val context: Context,
    private val orderId: String
) :
    RecyclerView.Adapter<CoffeeItemOrderPendingAdapter.CoffeeItemViewHolder>() {


    class CoffeeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coffeeImage: ImageView = itemView.findViewById(R.id.coffee_image)
        val coffeeTitle: TextView = itemView.findViewById(R.id.coffee_title)
        val categoryTitle: TextView = itemView.findViewById(R.id.category_title)
        val size: TextView = itemView.findViewById(R.id.size)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.coffee_item_order_pending, parent, false)

        return CoffeeItemViewHolder(itemView);
    }

    override fun getItemCount(): Int {
        return coffeeList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CoffeeItemViewHolder, position: Int) {
        val currentItem = coffeeList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.coffeePhotoUrl)
            .placeholder(R.drawable.caffe_mocha)
            .error(R.drawable.caffe_mocha)
            .into(holder.coffeeImage)

        holder.coffeeTitle.text = currentItem.coffeeTitle
        holder.categoryTitle.text = currentItem.categoryTitle
        holder.size.text = "Size: ${currentItem.size}"
        holder.quantity.text = "Quantity: ${currentItem.quantity}"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Detail::class.java).apply {
                putExtra("coffeeId", currentItem.coffeeId)
                putExtra("coffeeTitle", currentItem.coffeeTitle)
                putExtra("coffeePhotoUrl", currentItem.coffeePhotoUrl)
                putExtra("coffeeCost", currentItem.coffeeCost)
                putExtra("coffeeDescription", currentItem.coffeeDescription)
                putExtra("categoryTitle", currentItem.categoryTitle)
                putExtra("categoryId", currentItem.categoryId)

                putExtra("orderId", orderId)
            }
            context.startActivity(intent)
        }


    }
}