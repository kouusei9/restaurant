package com.kouusei.restaurant.presentation.favoriteview


sealed class FavoriteState {
    data object Loading : FavoriteState()
    data object Empty : FavoriteState()
    data class Error(val message: String) : FavoriteState()
    data class Success(val shops: List<FavoriteShopSummary>) : FavoriteState()
}