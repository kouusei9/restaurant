package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.data.api.entities.ShopName
import com.kouusei.restaurant.data.utils.ApiResult

interface HotPepperGourmetRepository {
    // search by location
    suspend fun searchShopsByLocation(
        lat: Double,
        lng: Double,
        range: Int,
        filters: Map<String, String>
    ): ApiResult<List<Shop>>

    // search by keyword
    suspend fun searchShopsByKeyword(
        keyword: String,
        filters: Map<String, String>
    ): ApiResult<List<Shop>>

    // search by combine
    suspend fun searchShops(
        keyword: String?,
        lat: Double?,
        lng: Double?,
        range: Int?,
        filters: Map<String, String>
    ): ApiResult<List<Shop>>

    suspend fun searchShopNames(
        keyword: String
    ): ApiResult<List<ShopName>>

    suspend fun shopDetailById(id: String): ApiResult<Shop>
}