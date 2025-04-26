package com.kouusei.restaurant.presentation.entities

import com.kouusei.restaurant.presentation.common.Filter

data class SearchFilters(
    val course: Boolean = false,
    val free_drink: Boolean = false,
    val free_food: Boolean = false,
    val private_room: Boolean = false,
    val wifi: Boolean = false,
    val card: Boolean = false,
    val nonSmoking: Boolean = false,
    val parking: Boolean = false,
    val lunch: Boolean = false,
    val english: Boolean = false,
    val pet: Boolean = false
) {
    fun getValue(filter: Filter): Boolean {
        when (filter) {
            Filter.Filter_FreeDrink -> return free_drink
            Filter.Filter_FreeFood -> return free_food
            Filter.Filter_PrivateRoom -> return private_room
            Filter.Filter_Wifi -> return wifi
            Filter.Filter_Course -> return course
            Filter.Filter_Card -> return card
            Filter.Filter_Non_Smoking -> return nonSmoking
            Filter.Filter_Parking -> return parking
            Filter.Filter_Lunch -> return lunch
            Filter.Filter_English -> return english
            Filter.Filter_Pet -> return pet
        }
    }

    fun toQueryMap(): Map<String, String> {
        return mapOf(
            "course" to if (course) "1" else "0",
            "free_drink" to if (free_drink) "1" else "0",
            "free_food" to if (free_food) "1" else "0",
            "private_room" to if (private_room) "1" else "0",
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