package com.kouusei.restaurant.data.api.entities

data class Address(
    val name: String,
    val code: String
)

data class LargeAreaResponse(
    val results: AddressLargeAreaResults
)

data class AddressLargeAreaResults(
    val large_area: List<Address>,
    val results_available: Int,
)

data class MiddleAreaResponse(
    val results: AddressMiddleAreaResults
)

data class AddressMiddleAreaResults(
    val middle_area: List<Address>,
    val results_available: Int,
)

data class SmallAreaResponse(
    val results: AddressSmallAreaResults
)

data class AddressSmallAreaResults(
    val small_area: List<Address>,
    val results_available: Int,
)