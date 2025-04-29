package com.kouusei.restaurant.presentation.favoriteview

import com.kouusei.restaurant.presentation.entities.ShopSummary

data class FavoriteShopSummary(
    val shopSummary: ShopSummary,
    val timestamp: Long
)