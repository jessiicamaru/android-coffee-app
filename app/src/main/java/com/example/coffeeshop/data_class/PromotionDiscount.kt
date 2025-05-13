package com.example.coffeeshop.data_class

import com.example.coffeeshop.constants.DiscountTarget
import com.example.coffeeshop.constants.DiscountType

data class PromotionDiscount(
    val id: Int,
    val discountTarget: DiscountTarget,
    val discountType: DiscountType,
    val discountValue: Double
)
