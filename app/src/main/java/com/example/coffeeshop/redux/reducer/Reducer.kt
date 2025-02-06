package com.example.coffeeshop.redux.reducer

import android.util.Log
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState

class Reducer {
    companion object {
        val appReducer: (AppState, Any) -> AppState = { state, action ->
            when (action) {
                is Action.SetCoffees -> state.copy(coffees = action.coffees)
                is Action.SetCategories -> state.copy(categories = action.categories)
                is Action.SelectCategory -> {
                    Log.d("REDUX_LOG", "${action.categoryId}")
                    state.copy(selectedCategory = action.categoryId)
                }
                is Action.AddOrder -> state.copy(orders = ArrayList(state.orders + action.coffee))
                else -> state
            }
        }
    }
}