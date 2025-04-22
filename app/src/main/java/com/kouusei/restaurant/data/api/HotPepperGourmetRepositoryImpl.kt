package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.model.Shop
import com.kouusei.restaurant.data.utils.ApiResult
import javax.inject.Inject

class HotPepperGourmetRepositoryImpl @Inject constructor(private val apiService: HotPepperGourmetService) :
    HotPepperGourmetRepository {
    override suspend fun searchShops(
        keyword: String,
        lat: Double,
        lng: Double
    ): ApiResult<List<Shop>> {
        return try {
            val response = apiService.gourmetSearch(keyword = keyword, lat = lat, lng = lng)
            ApiResult.Success(response.results.shop)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}