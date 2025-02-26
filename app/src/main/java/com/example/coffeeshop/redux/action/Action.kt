package com.example.coffeeshop.redux.action

import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.User

sealed class Action {
    data class SetAddress(val address: String?) : Action()
    data class SetCoffees(val coffees: ArrayList<Coffee>) : Action()
    data class SetCategories(val categories: ArrayList<Category>) : Action()
    data class SetLikeCoffees(val likeCoffees: ArrayList<String>) : Action()
    data class SelectCategory(val categoryId: String?) : Action()
    data class AddOrder(val coffee: Coffee, val quantity: Int = 1, val size: String) : Action()
    data class SaveUser(val user: User) : Action()
    data class RemoveOrder(val coffee: Coffee) : Action()
    data class RemoveLikeCoffee(val likeCoffee: Coffee) : Action()
    data class IncreaseOrderQuantity(val coffeeId: String, val size: String) : Action()
    data class DecreaseOrderQuantity(val coffeeId: String, val size: String) : Action()
    object RefreshOrders : Action()
}