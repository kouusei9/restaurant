package com.kouusei.restaurant.presentation

import com.google.android.gms.maps.model.LatLngBounds
import com.kouusei.restaurant.presentation.entities.ShopSummary

sealed class RestaurantViewState {
    data object RequestPermission : RestaurantViewState()
    data object Loading : RestaurantViewState()
    data class Success(
        val shopList: List<ShopSummary>,
        val boundingBox: LatLngBounds
    ) : RestaurantViewState()

    data class Error(val message: String) : RestaurantViewState()
}