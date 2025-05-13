package com.example.coffeeshop.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.activity.Detail
import com.example.coffeeshop.activity.OrderDetail
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.data_class.Promotion
import com.example.coffeeshop.data_class.PromotionResponse
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store

class PromotionItemAdapter(
    private val promotions: List<PromotionResponse>,
    private val context: Context
) :
    RecyclerView.Adapter<PromotionItemAdapter.OrderViewHolder>() {

    private val store = Store.store

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val icon: ImageView = itemView.findViewById(R.id.promotion_icon)
        val code: TextView = itemView.findViewById(R.id.promotion_code)
        val type: TextView = itemView.findViewById(R.id.promotion_type)
        val date: TextView = itemView.findViewById(R.id.exp_date)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.promotion_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val promotion = promotions[position]

        if (promotion.promotionType.equals("product")) {
            holder.icon.setBackgroundResource(R.drawable.ic_product)
        } else {
            holder.icon.setBackgroundResource(R.drawable.ic_delivery)
        }

        if (promotion.isInvalid == true) {
            holder.cardView.alpha = 0.5f
            holder.itemView.isEnabled = false
            holder.itemView.isClickable = false
            holder.checkbox.isClickable = false
            holder.checkbox.isEnabled = false
        } else {
            holder.cardView.alpha = 1f
            holder.itemView.isEnabled = true
            holder.itemView.isClickable = true
            holder.checkbox.isClickable = true
            holder.checkbox.isEnabled = true
            holder.itemView.setOnClickListener {}
        }

        holder.code.text = "Code: ${promotion.code}"
        holder.type.text = "Type: ${promotion.promotionType}"
        holder.date.text = "Expires: ${promotion.endDate}"

        holder.checkbox.isChecked =
            store.state.selectedPromotions.any { it.promotionId == promotion.promotionId }

        holder.checkbox.setOnClickListener {
            val isChecked = !holder.checkbox.isChecked
            holder.checkbox.isChecked = isChecked
            store.dispatch(
                Action.SetSelectedPromotion(
                    Promotion(
                        promotionId = promotion.promotionId,
                        promotionCode = promotion.code
                    )
                )
            )
            Log.d("Promotion", "Selected Promotions: ${store.state.selectedPromotions}")
        }
    }

    override fun getItemCount(): Int = promotions.size
}