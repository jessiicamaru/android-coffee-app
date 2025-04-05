package com.example.coffeeshop.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.activity.Detail
import com.example.coffeeshop.activity.OrderDetail
import com.example.coffeeshop.data_class.PendingOrder

class OrderAdapter(private val orders: List<PendingOrder>, private val context: Context) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.tv_order_id)
        val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        val userEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val createdAt: TextView = itemView.findViewById(R.id.tv_created_at)
        val total: TextView = itemView.findViewById(R.id.total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val totalAmount = order.coffees.sumOf { it.coffeeCost * it.quantity }
        holder.orderId.text = "Order ID: ${order.orderId}"
        holder.userName.text = "Name: ${order.userInfo.name}"
        holder.userEmail.text = "Email: ${order.userInfo.email}"
        holder.createdAt.text = "Created at: ${order.createdAt}"
        holder.total.text = "Total: $totalAmount"

        Log.d("ORDER_ID", order.orderId)
        Log.d("TOTAL_AMOUNT", "Calculated total: $totalAmount")

        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetail::class.java).apply {
                putExtra("orderId", order.orderId)
            }
            Log.d("INTENT", "Sending intent with orderId: ${order.orderId}, totalAmount: $totalAmount")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size
}