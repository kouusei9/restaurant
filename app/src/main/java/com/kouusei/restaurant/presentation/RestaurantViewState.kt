package com.kouusei.restaurant.presentation

import com.google.android.gms.maps.model.LatLngBounds
import com.kouusei.restaurant.presentation.entities.ShopSummary

sealed class RestaurantViewState {
    data object RequestPermission : RestaurantViewState()
    data object Loading : RestaurantViewState()
    data class Success(
        val shopList: List<ShopSummary>,
        val boundingBox: LatLngBounds,
        val totalSize: Int
    ) : RestaurantViewState()

    data object Empty : RestaurantViewState()
    data class Error(val message: String) : RestaurantViewState()
}