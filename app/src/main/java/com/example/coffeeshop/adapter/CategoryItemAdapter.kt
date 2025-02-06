package com.example.coffeeshop.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service

class CategoryItemAdapter(private val categoryList: ArrayList<Category>) :
    RecyclerView.Adapter<CategoryItemAdapter.CategoryItemViewHolder>() {

    private var store = Store.Companion.store;
    private var service = Service();
    private var selectedCategory: String? = store.state.selectedCategory

    class CategoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTitle: TextView = itemView.findViewById(R.id.category_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)

        return CategoryItemViewHolder(itemView);
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryItemViewHolder, position: Int) {
        val currentItem = categoryList[position]
        holder.categoryTitle.text = currentItem.categoryTitle

        val isSelected = store.state.selectedCategory == currentItem.categoryId
        holder.categoryTitle.setBackgroundResource(
            if (isSelected) R.drawable.button_primary else R.drawable.button_unselected
        )

        // Trong Adapter của Category
        holder.itemView.setOnClickListener {
            Log.d("CATEGORY-LOG", selectedCategory + " " + currentItem.categoryId )
            onCategorySelected(currentItem.categoryId)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onCategorySelected(categoryId: String) {
//        if (categoryId == "all") {
//            service.getAllCoffees()
//        }
        Log.d("CATEGORY-LOGSHIT", categoryId)
        store.dispatch(Action.SelectCategory(categoryId)) // Lọc theo danh mục
        selectedCategory = if (categoryId == "all") null else categoryId // Đặt null nếu chọn "all"
        notifyDataSetChanged()
    }


}