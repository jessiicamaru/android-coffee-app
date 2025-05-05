package com.example.coffeeshop.data_class

import java.time.LocalDateTime

data class PromotionResponse(
    val promotionId: String,
    val code: String,
    val name: String,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val status: String,
    val promotionType: String,
    val usageLimitPerUser: Int,
    val conditions: List<PromotionCondition>,
    val discounts: List<PromotionDiscount>,
    val eligibleUsers: List<PromotionEligibleUser>,
    var isInvalid: Boolean? = false
)