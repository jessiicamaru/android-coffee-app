package com.example.coffeeshop.data_class

import com.example.coffeeshop.constants.CriteriaType
import com.example.coffeeshop.constants.Operator

data class PromotionEligibleUser(
    val id: Int,
    val criteriaType: CriteriaType,
    val operator: Operator,
    val criteriaValue: String
)
