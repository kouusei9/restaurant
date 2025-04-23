package com.kouusei.restaurant.presentation

import com.kouusei.restaurant.presentation.entities.ShopDetail

sealed class DetailViewState {
    data object Loading : DetailViewState()
    data class Success(val shopDetail: ShopDetail) : DetailViewState()
    data class Error(val message: String) : DetailViewState()
}