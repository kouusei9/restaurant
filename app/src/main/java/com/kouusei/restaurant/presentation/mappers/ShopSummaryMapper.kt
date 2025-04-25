package com.kouusei.restaurant.presentation.mappers

import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.presentation.entities.ShopSummary


fun Shop.toShopSummary(): ShopSummary {
    return ShopSummary(
        id = id,
        name = name,
        url = photo.mobile.s,
        budget = if (budget.name.isNotEmpty()) budget.name else budget.average,
        access = mobile_access,
        location = LatLng(lat, lng)
    )
}