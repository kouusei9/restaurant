package com.kouusei.restaurant.data.api.entities


data class ShopNameResponse(
    val results: ShopNameResults
)

data class ShopNameResults(
    val shop: List<ShopName>,
    val results_available: Int,
    val error: Error?
)

data class Error(
    val code: Int,
    val message: String
)

data class ShopName(
    val id: String,
    val name: String,
    val desc: Int
)