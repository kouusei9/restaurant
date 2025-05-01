package com.kouusei.restaurant.presentation.entities

import com.kouusei.restaurant.data.api.entities.Address

sealed class AddressState {
    object None : AddressState()
    data class Success(
        val largeAddress: Address?,
        val middleAddress: Address?,
        val smallAddress: Address?
    ) : AddressState()
}