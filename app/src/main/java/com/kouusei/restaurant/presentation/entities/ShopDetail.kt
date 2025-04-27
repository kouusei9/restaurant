package com.kouusei.restaurant.presentation.entities

import com.google.android.gms.maps.model.LatLng

class ShopDetail(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val openTime: String,
    val closeTime: String,
    val url: String,
    val budget: String,
    val access: String,
    val location: LatLng,
    val catch: String,
    val genre: String,
    val course: Boolean,
    val freeDrink: Boolean,
    val freeFood: Boolean,
    val card: Boolean,
    val show: Boolean,
    val lunch: Boolean,
    val english: Boolean,
    val pet: Boolean,
    val nonSmoking: String,
    val wifi: String,
    val child: String,
    val midNight: String,
    val barrierFree: String,
    val parking: String,
    val privateRoom: String,
    val horigotatsu: String
)