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
        return mapOf(
            "course" to if (course) "1" else "0",
            "free_drink" to if (freeDrink) "1" else "0",
            "free_food" to if (freeFood) "1" else "0",
            "private_room" to if (privateRoom) "1" else "0",
            "wifi" to if (wifi) "1" else "0",
            "card" to if (card) "1" else "0",
            "non_smoking" to if (nonSmoking) "1" else "0",
            "parking" to if (parking) "1" else "0",
            "lunch" to if (lunch) "1" else "0",
            "english" to if (english) "1" else "0",
            "pet" to if (pet) "1" else "0",
        )
    }
}