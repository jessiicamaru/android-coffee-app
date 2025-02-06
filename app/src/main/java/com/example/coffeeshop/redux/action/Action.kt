package com.example.coffeeshop.redux.action

import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee

sealed class Action {
    data class SetCoffees(val coffees: ArrayList<Coffee>) : Action()
    data class SetCategories(val categories: ArrayList<Category>) : Action()
    data class SelectCategory(val categoryId: String?) : Action()
    data class AddOrder(val coffee: Coffee) : Action()
}