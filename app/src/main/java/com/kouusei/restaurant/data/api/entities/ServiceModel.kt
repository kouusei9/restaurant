package com.kouusei.restaurant.data.api.entities

data class ShopResponse(
    val results: Results
)

data class Results(
    val shop: List<Shop>,
    val results_available: Int,
    val error: Error?
)

data class Shop(
    val id: String,
    val name: String,
    val address: String,
    val genre: Genre,
    val logo_image: String?,
    val photo: Photo,
    val budget: Budget,
    val catch: String,
    val access: String,
    val mobile_access: String,
    val open: String,
    val close: String,
    val lat: Double,
    val lng: Double,
    val capacity: Int,
)

data class Genre(
    val name: String,
    val catch: String
)

data class Budget(
    val name: String,
    val average: String
)

data class Photo(
    val mobile: MobilePhoto
)

data class MobilePhoto(
    val l: String,
    val s: String
)