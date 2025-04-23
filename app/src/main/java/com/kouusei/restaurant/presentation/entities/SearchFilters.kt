package com.kouusei.restaurant.presentation.entities

import com.kouusei.restaurant.presentation.common.Filter

data class SearchFilters(
    val course: Boolean = false,
    val free_drink: Boolean = false,
    val free_food: Boolean = false,
    val private_room: Boolean = false,
//    val midnight_meal: Boolean = false,
//    val english: Boolean = false,
//    val pet: Boolean = false
) {
    fun getValue(filter: Filter): Boolean {
        when (filter) {
            Filter.Filter_FreeDrink -> return free_drink
            Filter.Filter_FreeFood -> return free_food
            Filter.Filter_PrivateRoom -> return private_room
        }
    }

    fun toQueryMap(): Map<String, String> {
        return mapOf(
            "course" to if (course) "1" else "0",
            "free_drink" to if (free_drink) "1" else "0",
            "free_food" to if (free_food) "1" else "0",
            "private_room" to if (private_room) "1" else "0",
//            "midnight_meal" to if (midnight_meal) "1" else "0",
//            "english" to if (english) "1" else "0",
//            "pet" to if (pet) "1" else "0"
        )
    }
}