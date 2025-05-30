package com.example.coffeeshop.redux.action

import android.app.Activity
import com.example.coffeeshop.data_class.Category
import com.example.coffeeshop.data_class.Coffee
import com.example.coffeeshop.data_class.LocationData
import com.example.coffeeshop.data_class.PendingOrder
import com.example.coffeeshop.data_class.Promotion
import com.example.coffeeshop.data_class.PromotionResponse
import com.example.coffeeshop.data_class.SocketResponse
import com.example.coffeeshop.data_class.User
import org.json.JSONArray

sealed class Action {
    data class SetAddress(val address: String?) : Action()
    data class SetCoffees(val coffees: ArrayList<Coffee>) : Action()
    data class SetCategories(val categories: ArrayList<Category>) : Action()
    data class SetLikeCoffees(val likeCoffees: ArrayList<String>) : Action()
    data class SelectCategory(val categoryId: String?) : Action()
    data class AddOrder(val coffee: Coffee, val quantity: Int = 1, val size: String) : Action()
    data class SetOrders(val orderRequest: ArrayList<PendingOrder>): Action()
    data class SaveUser(val user: User) : Action()
    data class RemoveOrder(val coffee: Coffee) : Action()
    data class RemoveLikeCoffee(val likeCoffee: Coffee) : Action()
    data class IncreaseOrderQuantity(val coffeeId: String, val size: String) : Action()
    data class DecreaseOrderQuantity(val coffeeId: String, val size: String) : Action()
    data class AddHistory(val history: Activity) : Action()
    data class SetLocation(val location: LocationData) : Action()
    data class SetMapData(val mapData: JSONArray): Action()
    data class SetPromotions(val promotions: ArrayList<PromotionResponse>) : Action()
    data class SetInvalidPromotions(val invalidPromotionIds: ArrayList<String>): Action()
    data class ModifyPromotions(val invalidPromotionIds: ArrayList<String>): Action()
    data class SetNotifications(val socketResponse: SocketResponse): Action()
    data class UpdateStat(val socketResponse: SocketResponse): Action()
    data class SetSelectedPromotion(val promotion: Promotion): Action()
    data class SetShippingData(val shippingFee: Double, val distanceKm: Double) : Action()
    data class SetNote(val note: String?) : Action()
    data class SetReceiveCustomer(val receiveCustomer: String?) : Action()
    data class SetTempLocation(val location: LocationData?) : Action()
    data class SetTempAddress(val address: String?) : Action()
    data object RemoveHistory : Action()
    data object RemoveOrderInfo: Action()
    data object RemoveTempLocation: Action()
    data object RemoveCart: Action()
    data object RefreshOrdersPending: Action()
    data object RefreshOrders : Action()
}