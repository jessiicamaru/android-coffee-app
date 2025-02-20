package com.example.coffeeshop.redux.reducer

import android.util.Log
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState

class Reducer {
    companion object {
        val appReducer: (AppState, Any) -> AppState = { state, action ->
            when (action) {
                is Action.SetCoffees -> state.copy(coffees = action.coffees)
                is Action.SetAddress -> state.copy(address = action.address)
                is Action.SetCategories -> state.copy(categories = action.categories)
                is Action.SelectCategory -> {
                    Log.d("REDUX_LOGSHIT", "${action.categoryId}")
                    state.copy(selectedCategory = action.categoryId)
                }
//                is Action.AddOrder -> {
//                    val newOrders = state.orders.toMutableList()
//                    newOrders.add(action.coffee)
//                    state.copy(orders = newOrders)
//                }

                is Action.AddOrder -> {
                    val existingOrder = state.orders.find { it.coffeeId == action.coffee.coffeeId }

                    val updatedOrders = if (existingOrder != null) {
                        state.orders.map { coffee ->
                            if (coffee.coffeeId == action.coffee.coffeeId) {
                                coffee.copy(quantity = coffee.quantity + action.quantity)
                            } else coffee
                        }
                    } else {
                        state.orders + action.coffee.copy(quantity = action.quantity)
                    }

                    state.copy(orders = ArrayList(updatedOrders))
                }


                is Action.SaveUser -> state.copy(user = action.user)
                is Action.RefreshOrders -> state.copy(orders = ArrayList(state.orders))
                is Action.RemoveOrder -> {
                    state.copy(orders = ArrayList(state.orders.filterNot { it == action.coffee }))
                }

                is Action.IncreaseOrderQuantity -> {
                    val updatedOrders = state.orders.map { coffee ->
                        if (coffee.coffeeId == action.coffeeId) {
                            coffee.copy(quantity = coffee.quantity + 1)  // Tăng số lượng
                        } else coffee
                    }
                    state.copy(orders = ArrayList(updatedOrders))
                }

                is Action.DecreaseOrderQuantity -> {
                    val updatedOrders = state.orders.mapNotNull { coffee ->
                        when {
                            coffee.coffeeId == action.coffeeId && coffee.quantity > 1 ->
                                coffee.copy(quantity = coffee.quantity - 1)
                            coffee.coffeeId == action.coffeeId && coffee.quantity == 1 ->
                                null
                            else -> coffee
                        }
                    }
                    state.copy(orders = ArrayList(updatedOrders))
                }
                else -> state
            }
        }
    }
}