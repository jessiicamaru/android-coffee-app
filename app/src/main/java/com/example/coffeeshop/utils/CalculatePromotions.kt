package com.example.coffeeshop.utils

import android.annotation.SuppressLint
import com.example.coffeeshop.constants.ConditionType
import com.example.coffeeshop.constants.DiscountTarget
import com.example.coffeeshop.constants.DiscountType
import com.example.coffeeshop.constants.Operator
import com.example.coffeeshop.constants.PromotionStatus
import com.example.coffeeshop.data_class.PromotionResponse
import java.time.LocalDateTime

@SuppressLint("NewApi")
fun calculatePromotions(
    price: Double,
    distanceKm: Double?,
    promotions: List<PromotionResponse>
): Pair<Double, Double> {
    var discountedPrice = price
    var discountedFee = if (distanceKm != null) distanceKm * 0.3 else 0.0

    val currentDateTime = LocalDateTime.now() // 10:40 PM +07, May 13, 2025

    promotions.forEach { promotion ->
        if (promotion.status != PromotionStatus.active ||
            currentDateTime.isBefore(promotion.startDate) ||
            currentDateTime.isAfter(promotion.endDate)
        ) {
            return@forEach
        }

        // Kiểm tra điều kiện áp dụng
        val isConditionMet = promotion.conditions.all { condition ->
            when (ConditionType.valueOf(condition.conditionType.name)) {
                ConditionType.totalOrderValue -> {
                    when (Operator.valueOf(condition.operator.name)) {
                        Operator.greaterThan -> price > condition.conditionValue
                        Operator.equal -> price == condition.conditionValue
                        Operator.lessThan -> price < condition.conditionValue
                    }
                }
                ConditionType.distance -> {
                    distanceKm?.let { dist ->
                        when (Operator.valueOf(condition.operator.name)) {
                            Operator.greaterThan -> dist > condition.conditionValue
                            Operator.equal -> dist == condition.conditionValue
                            Operator.lessThan -> dist < condition.conditionValue
                        }
                    } ?: false
                }
                else -> true // Bỏ qua các điều kiện không hỗ trợ
            }
        }

        // Áp dụng giảm giá nếu thỏa mãn điều kiện
        if (isConditionMet) {
            promotion.discounts.forEach { discount ->
                when (DiscountTarget.valueOf(discount.discountTarget.name)) {
                    DiscountTarget.totalOrder -> {
                        when (DiscountType.valueOf(discount.discountType.name)) {
                            DiscountType.percentage -> discountedPrice -= (discountedPrice * discount.discountValue / 100)
                            DiscountType.fixedAmount -> discountedPrice -= discount.discountValue
                            DiscountType.free -> discountedPrice = 0.0 // Không áp dụng vì target là totalOrder
                        }
                    }
                    DiscountTarget.shippingFee -> {
                        when (DiscountType.valueOf(discount.discountType.name)) {
                            DiscountType.percentage -> discountedFee -= (discountedFee * discount.discountValue / 100)
                            DiscountType.fixedAmount -> discountedFee -= discount.discountValue
                            DiscountType.free -> discountedFee = 0.0
                        }
                    }
                    DiscountTarget.totalProduct -> {
                    }
                }
            }
        }
    }

    discountedPrice = maxOf(discountedPrice, 0.0)
    discountedFee = maxOf(discountedFee, 0.0)

    return Pair(discountedPrice, discountedFee)
}