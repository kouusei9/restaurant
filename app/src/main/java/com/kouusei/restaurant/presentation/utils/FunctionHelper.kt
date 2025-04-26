package com.kouusei.restaurant.presentation.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng


fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun splitBusinessHours(raw: String): List<Pair<String, String>> {
    val regex =
        Regex("""([月火水木金土日祝前日、～]+): (.*?)(?=([月火水木金土日祝前日、～]+):|${'$'})""")
    val matches = regex.findAll(raw)
    return matches.map { matchResult ->
        val dayPart = matchResult.groupValues[1]
        val timePart = matchResult.groupValues[2]
            .replace("（", "\n").replace("）", "")
        Pair(dayPart, timePart)
    }.toList()
}