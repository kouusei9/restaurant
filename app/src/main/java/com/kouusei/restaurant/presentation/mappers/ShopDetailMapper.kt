package com.kouusei.restaurant.presentation.mappers

import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.presentation.entities.ShopDetail

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
        closeTime = close,
        catch = catch,
        genre = genre.name,
        course = checkIsOk(course),
        freeDrink = checkIsOk(free_drink),
        freeFood = checkIsOk(free_food),
        smoking = checkIsOk(non_smoking),
        card = checkIsOk(card),
        show = checkIsOk(show),
        lunch = checkIsOk(lunch),
        english = checkIsOk(english),
        pet = checkIsOk(pet),
        wifi = wifi,
        child = child,
        midNight = midnight,
        barrierFree = barrier_free,
        parking = parking,
        privateRoom = private_room,
    )
}

fun checkIsOk(string: String): Boolean {
    return !(string.contains("なし") || string.contains("不可"))
}