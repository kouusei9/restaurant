package com.kouusei.restaurant.presentation.mappers

import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.presentation.entities.ShopDetail

fun Shop.toShopDetail(): ShopDetail {
    return ShopDetail(
        id = id,
        name = name,
        url = photo.mobile.s,
        budget = if (budget.name.isNotEmpty()) budget.name else budget.average,
        access = mobile_access,
        location = LatLng(lat, lng),
        logoUrl = logo_image,
        openTime = open,
        closeTime = close,
        catch = catch,
        genre = genre.name,
        course = checkIsOk(course),
        freeDrink = checkIsOk(free_drink),
        freeFood = checkIsOk(free_food),
        nonSmoking = non_smoking ?: "未確認",
        card = checkIsOk(card),
        show = checkIsOk(show),
        lunch = checkIsOk(lunch),
        english = checkIsOk(english),
        pet = checkIsOk(pet),
        wifi = wifi ?: "未確認",
        child = child ?: "未確認",
        midNight = midnight ?: "未確認",
        barrierFree = barrier_free ?: "未確認",
        parking = parking ?: "未確認",
        privateRoom = private_room ?: "未確認",
        horigotatsu = horigotatsu ?: "未確認"
    )
}

fun checkIsOk(string: String?): Boolean {
    if (string == null) {
        return false
    }
    return !(string.contains("なし") || string.contains("不可"))
}