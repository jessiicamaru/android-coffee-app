package com.example.coffeeshop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CategoryItemAdapter
import com.example.coffeeshop.adapter.CoffeeItemAdapter
import com.example.coffeeshop.decoration.ItemMarginRight
import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.store.Store
import com.example.coffeeshop.service.Service

class Home : Activity() {

    private lateinit var locationText: TextView

    private lateinit var coffeeRecyclerView: RecyclerView

    private lateinit var categoryRecyclerView: RecyclerView

    private var store = Store.Companion.store;
    private var service = Service();

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        locationText = findViewById(R.id.location) // Lấy view TextView để hiển thị địa chỉ


        coffeeRecyclerView = findViewById(R.id.recycler_view)
        coffeeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        coffeeRecyclerView.setHasFixedSize(true)

        categoryRecyclerView = findViewById(R.id.category_recycler_view)
        categoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        categoryRecyclerView.addItemDecoration(ItemMarginRight(spacing))

        store.subscribe {
            val layoutManager = categoryRecyclerView.layoutManager
            val scrollPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            updateUI(store.state)
            layoutManager.scrollToPosition(scrollPosition)
        }


        service.getAllCoffees()
        service.getAllCategories()

    }



    private fun updateUI(state: AppState) {
        // Cập nhật UI dựa trên state hiện tại
        coffeeRecyclerView.adapter = CoffeeItemAdapter(state.coffees, this)
        categoryRecyclerView.adapter = CategoryItemAdapter(state.categories)

        locationText.text = store.state.address

        val filteredCoffees = if (state.selectedCategory == "all") {
            state.coffees // Hiển thị toàn bộ cà phê
        } else {
            state.coffees.filter { it.categoryId == state.selectedCategory }
        }
        coffeeRecyclerView.adapter = CoffeeItemAdapter(ArrayList(filteredCoffees), this)
    }





}
