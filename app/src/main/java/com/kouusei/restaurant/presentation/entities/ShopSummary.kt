package com.kouusei.restaurant.presentation.entities

import com.google.android.gms.maps.model.LatLng

data class ShopSummary(
    val id: String,
    val name: String,
    val url: String,
    val budget: String,
    val access: String,
    val location: LatLng
)