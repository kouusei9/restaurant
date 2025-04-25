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
    val capacity: String?,
    val course: String?,
    val free_drink: String?,
    val free_food: String?,
    val private_room: String?,
    val horigotatsu: String?,
    val tatami: String?,
    val card: String?,
    val non_smoking: String?,
    val charter: String?,
    val parking: String?,
    val barrier_free: String?,
    val show: String?,
    val karaoke: String?,
    val band: String?,
    val tv: String?,
    val lunch: String?,
    val midnight: String?,
    val english: String?,
    val pet: String?,
    val child: String?,
    val wifi: String?,
    val sp: String?
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