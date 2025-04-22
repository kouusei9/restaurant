package com.kouusei.restaurant.presentation.mappers

import com.kouusei.restaurant.data.api.model.Shop
import com.kouusei.restaurant.presentation.entities.ShopSummary

class ShopSummaryMapper {
}

fun Shop.toShopSummary(): ShopSummary {
    return ShopSummary(
        id = id,
        name = name,
        url = photo.mobile.s,
        budget = budget.name,
        access = mobile_access
    )
}