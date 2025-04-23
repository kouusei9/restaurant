package com.kouusei.restaurant.presentation.entities

import com.google.android.gms.maps.model.LatLng

class ShopDetail(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val openTime: String,
    val url: String,
    val budget: String,
    val access: String,
    val location: LatLng,
    val catch: String
) {
}