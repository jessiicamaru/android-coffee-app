package com.example.coffeeshop.data_class

import com.example.coffeeshop.constants.ConditionType
import com.example.coffeeshop.constants.Operator


data class PromotionCondition(
    val id: Int,
    val conditionType: ConditionType,
    val operator: Operator,
    val conditionValue: Double
)