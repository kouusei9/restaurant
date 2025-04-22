package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.model.Shop
import com.kouusei.restaurant.data.utils.ApiResult

interface HotPepperGourmetRepository {
    suspend fun searchShops(
        keyword: String,
        lat: Double,
        lng: Double
    ): ApiResult<List<Shop>>
}