package com.kouusei.restaurant

import kotlinx.serialization.Serializable

interface Route {
    val route: String
}

@Serializable
object Map : Route {
    override val route = "map"
}

@Serializable
object List : Route {
    override val route = "list"
}

@Serializable
object Favorites : Route {
    override val route = "favorites"
}

@Serializable
data class Detail(val id: String) : Route {
    override val route = "detail/${id}"
}