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

class CoffeeItemCartAdapter(
    private val coffeeList: ArrayList<Coffee>,
    private val context: Context
) :
    RecyclerView.Adapter<CoffeeItemCartAdapter.CoffeeItemViewHolder>() {

    private val store = Store.store;

    class CoffeeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coffeeImage: ImageView = itemView.findViewById(R.id.coffee_image)
        val coffeeTitle: TextView = itemView.findViewById(R.id.coffee_title)
        val categoryTitle: TextView = itemView.findViewById(R.id.category_title)
        val coffeeCost: TextView = itemView.findViewById(R.id.coffee_cost)
//        val removeButton: Button = itemView.findViewById(R.id.remove_button)
        val increaseButton: Button = itemView.findViewById(R.id.increase_button)
        val decreaseButton: Button = itemView.findViewById(R.id.decrease_button)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.coffee_item_cart, parent, false)

        return CoffeeItemViewHolder(itemView);
    }

    override fun getItemCount(): Int {
        return coffeeList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CoffeeItemViewHolder, position: Int) {
        val currentItem = coffeeList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.coffeePhotoUrl) // URL ảnh
            .placeholder(R.drawable.caffe_mocha) // Ảnh placeholder khi đang tải
            .error(R.drawable.caffe_mocha) // Ảnh hiển thị nếu tải ảnh thất bại
            .into(holder.coffeeImage) // ImageView cần hiển thị ảnh

        // Gán dữ liệu khác
        holder.coffeeTitle.text = currentItem.coffeeTitle
        holder.categoryTitle.text = currentItem.categoryTitle
        holder.coffeeCost.text = "$ ${currentItem.coffeeCost}";
        holder.quantity.text = "${currentItem.quantity}";

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Detail::class.java).apply {
                putExtra("coffeeId", currentItem.coffeeId)
                putExtra("coffeeTitle", currentItem.coffeeTitle)
                putExtra("coffeePhotoUrl", currentItem.coffeePhotoUrl)
                putExtra("coffeeCost", currentItem.coffeeCost)
                putExtra("coffeeDescription", currentItem.coffeeDescription)
                putExtra("categoryTitle", currentItem.categoryTitle)
            }
            context.startActivity(intent)
        }

//        holder.removeButton.setOnClickListener {
//            store.dispatch(
//                Action.RemoveOrder(
//                    Coffee(
//                        coffeeId = currentItem.coffeeId,
//                        coffeeTitle = currentItem.coffeeTitle,
//                        coffeePhotoUrl = currentItem.coffeePhotoUrl,
//                        coffeeCost = currentItem.coffeeCost,
//                        coffeeDescription = currentItem.coffeeDescription,
//                        categoryTitle = currentItem.categoryTitle,
//                        categoryId = currentItem.categoryId,
//                    )
//                )
//            )
//        }

        holder.increaseButton.setOnClickListener {
            store.dispatch(Action.IncreaseOrderQuantity(currentItem.coffeeId))
        }

        holder.decreaseButton.setOnClickListener {
            store.dispatch(Action.DecreaseOrderQuantity(currentItem.coffeeId))
        }
    }
}