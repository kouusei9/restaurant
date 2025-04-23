package com.kouusei.restaurant.presentation.mappers

import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.presentation.entities.ShopDetail

class ShopDetailMapper {
}

fun Shop.toShopDetail(): ShopDetail {
    return ShopDetail(
        id = id,
        name = name,
        url = photo.mobile.s,
        budget = budget.name,
        access = mobile_access,
        location = LatLng(lat, lng),
        logoUrl = logo_image,
        openTime = open,
        catch = catch
    )
}