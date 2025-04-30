package com.kouusei.restaurant.presentation.entities

import com.kouusei.restaurant.presentation.common.Filter

data class SearchFilters(
    val course: Boolean = false,
    val freeDrink: Boolean = false,
    val freeFood: Boolean = false,
    val privateRoom: Boolean = false,
    val wifi: Boolean = false,
    val card: Boolean = false,
    val nonSmoking: Boolean = false,
    val parking: Boolean = false,
    val lunch: Boolean = false,
    val english: Boolean = false,
    val pet: Boolean = false
) {
    fun getValue(filter: Filter): Boolean {
        return when (filter) {
            Filter.Filter_FreeDrink -> freeDrink
            Filter.Filter_FreeFood -> freeFood
            Filter.Filter_PrivateRoom -> privateRoom
            Filter.Filter_Wifi -> wifi
            Filter.Filter_Course -> course
            Filter.Filter_Card -> card
            Filter.Filter_Non_Smoking -> nonSmoking
            Filter.Filter_Parking -> parking
            Filter.Filter_Lunch -> lunch
            Filter.Filter_English -> english
            Filter.Filter_Pet -> pet
        }
    }

    fun toQueryMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (course) map["course"] = "1"
        if (freeDrink) map["free_drink"] = "1"
        if (freeFood) map["free_food"] = "1"
        if (privateRoom) map["private_room"] = "1"
        if (wifi) map["wifi"] = "1"
        if (card) map["card"] = "1"
        if (nonSmoking) map["non_smoking"] = "1"
        if (parking) map["parking"] = "1"
        if (lunch) map["lunch"] = "1"
        if (english) map["english"] = "1"
        if (pet) map["pet"] = "1"
        return map
    }
}