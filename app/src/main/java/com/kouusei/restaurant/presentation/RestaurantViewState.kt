package com.kouusei.restaurant.presentation

import com.kouusei.restaurant.presentation.entities.ShopSummary

sealed class RestaurantViewState {
    data object RequestPermission : RestaurantViewState()
    data object Loading : RestaurantViewState()
    data class Success(val shopList: List<ShopSummary>, val lat: Double, val lng: Double) :
        RestaurantViewState()

    data class Error(val message: String) : RestaurantViewState()
}