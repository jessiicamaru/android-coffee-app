package com.example.coffeeshop.redux.reducer

import android.util.Log
import com.example.coffeeshop.redux.action.Action
import com.example.coffeeshop.redux.data_class.AppState

class Reducer {
    companion object {
        val appReducer: (AppState, Any) -> AppState = { state, action ->
            when (action) {
                is Action.SetCoffees -> state.copy(coffees = action.coffees)
                is Action.SetLikeCoffees -> {
                    Log.d("SHIT_FAV", "redux: ${action.likeCoffees}")
                    state.copy(likeCoffees = action.likeCoffees)
                }
                is Action.SetAddress -> state.copy(address = action.address)
                is Action.SetCategories -> state.copy(categories = action.categories)
                is Action.SelectCategory -> {
                    Log.d("REDUX_LOGSHIT", "${action.categoryId}")
                    state.copy(selectedCategory = action.categoryId)
                }

                is Action.AddOrder -> {
                    val existingOrder = state.orders.find {
                        it.coffeeId == action.coffee.coffeeId && it.size == action.size
                    }
                    val updatedOrders = if (existingOrder != null) {
                        state.orders.map { coffee ->
                            if (coffee.coffeeId == action.coffee.coffeeId && coffee.size == action.size) {
                                coffee.copy(quantity = coffee.quantity + action.quantity)
                            } else {
                                coffee
                            }
                        }
                    } else {
                        state.orders + action.coffee.copy(quantity = action.quantity, size = action.size, coffeeCost = action.coffee.coffeeCost)
                    }
                    state.copy(orders = ArrayList(updatedOrders))
                }
                is Action.SetLocation -> state.copy(location = action.location)
                is Action.SaveUser -> state.copy(user = action.user)
                is Action.RefreshOrders -> state.copy(orders = ArrayList(state.orders))
                is Action.RefreshOrdersPending -> state.copy(ordersPending = ArrayList(state.ordersPending))
                is Action.RemoveOrder -> {
                    state.copy(orders = ArrayList(state.orders.filterNot { it == action.coffee }))
                }

                is Action.RemoveLikeCoffee -> {
                    state.copy(likeCoffees = ArrayList(state.likeCoffees.filterNot { it == action.likeCoffee.coffeeId }))
                }

                is Action.IncreaseOrderQuantity -> {
                    val updatedOrders = state.orders.map { coffee ->
                        if (coffee.coffeeId == action.coffeeId && coffee.size == action.size) {
                            coffee.copy(quantity = coffee.quantity + 1)  // Tăng số lượng
                        } else coffee
                    }
                    state.copy(orders = ArrayList(updatedOrders))
                }

                is Action.DecreaseOrderQuantity -> {
                    val updatedOrders = state.orders.mapNotNull { coffee ->
                        when {
                            coffee.coffeeId == action.coffeeId && coffee.size == action.size && coffee.quantity > 1 ->
                                coffee.copy(quantity = coffee.quantity - 1)
                            coffee.coffeeId == action.coffeeId && coffee.size == action.size && coffee.quantity == 1 ->
                                null
                            else -> coffee
                        }
                    }
                    state.copy(orders = ArrayList(updatedOrders))
                }

                is Action.AddHistory -> {
                    val updateHistory = state.historyList;
                    updateHistory.add(action.history)

                    state.copy(historyList = ArrayList(updateHistory))
                }

                is Action.RemoveHistory -> {
                    val updateHistory = state.historyList;
                    updateHistory.removeAt(updateHistory.size - 1)

                    state.copy(historyList = ArrayList(updateHistory))
                }

                is Action.SetOrders -> state.copy(ordersPending = action.orderRequest)
                else -> state
            }
        }
    }
}