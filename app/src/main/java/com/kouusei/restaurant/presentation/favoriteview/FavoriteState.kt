package com.kouusei.restaurant.presentation.favoriteview

import com.kouusei.restaurant.presentation.entities.ShopSummary


sealed class FavoriteState {
    data object Loading : FavoriteState()
    data object Empty : FavoriteState()
    data class Error(val message: String) : FavoriteState()
    data class Success(val shops: List<ShopSummary>) : FavoriteState()
}