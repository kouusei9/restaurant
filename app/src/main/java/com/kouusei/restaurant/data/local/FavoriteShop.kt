package com.kouusei.restaurant.data.local

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteShop(
    val shopId: String,
    val timestamp: Long
)
