package com.kouusei.restaurant.presentation.mappers

import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.presentation.entities.ShopSummary

class ShopSummaryMapper {
}

fun Shop.toShopSummary(): ShopSummary {
    return ShopSummary(
        id = id,
        name = name,
        url = photo.mobile.s,
        budget = budget.name,
        access = mobile_access,
        location = LatLng(lat, lng)
    )
}