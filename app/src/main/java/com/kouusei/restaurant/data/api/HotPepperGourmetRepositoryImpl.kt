package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.data.utils.ApiResult
import javax.inject.Inject

class HotPepperGourmetRepositoryImpl @Inject constructor(private val apiService: HotPepperGourmetService) :
    HotPepperGourmetRepository {

    override suspend fun searchShopsByLocation(
        lat: Double,
        lng: Double,
        range: Int,
        filters: Map<String, String>
    ): ApiResult<List<Shop>> {
        return try {
            val response =
                apiService.gourmetSearch(lat = lat, lng = lng, range = range, filters = filters)
            ApiResult.Success(response.results.shop)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun searchShopsByKeyword(
        keyword: String,
        filters: Map<String, String>
    ): ApiResult<List<Shop>> {
        if (keyword.isEmpty())
            return ApiResult.Success(emptyList())
        return try {
            val response = apiService.gourmetSearch(keyword = keyword, filters = filters)
            ApiResult.Success(response.results.shop)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun searchShops(
        keyword: String?,
        lat: Double?,
        lng: Double?,
        range: Int?,
        filters: Map<String, String>
    ): ApiResult<List<Shop>> {
        return try {
            val response =
                apiService.gourmetSearch(
                    keyword = keyword,
                    lat = lat,
                    lng = lng,
                    range = range,
                    filters = filters
                )
            ApiResult.Success(response.results.shop)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

}