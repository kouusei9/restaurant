package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.Results
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.data.api.entities.ShopName
import com.kouusei.restaurant.data.utils.ApiResult
import javax.inject.Inject

class HotPepperGourmetRepositoryImpl @Inject constructor(private val apiService: HotPepperGourmetService) :
    HotPepperGourmetRepository {

    override suspend fun searchShops(
        keyword: String?,
        lat: Double?,
        lng: Double?,
        range: Int?,
        start: Int,
        order: Int?,
        filters: Map<String, String>
    ): ApiResult<Results> {
        return try {
            val response =
                apiService.gourmetSearch(
                    keyword = keyword,
                    lat = lat,
                    lng = lng,
                    range = range,
                    filters = filters,
                    start = start,
                    order = order
                )
            if (response.results.error != null) {
                ApiResult.Error(response.results.error.message)
            } else {
                ApiResult.Success(response.results)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun searchShopNames(keyword: String): ApiResult<List<ShopName>> {
        return try {
            val response = apiService.shopNameSearch(keyword = keyword)
            if (response.results.error != null) {
                ApiResult.Error(response.results.error.message)
            } else {
                ApiResult.Success(response.results.shop)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun shopDetailById(id: String): ApiResult<Shop> {
        return try {
            val response =
                apiService.gourmetSearch(id = id)
            if (response.results.error != null) {
                ApiResult.Error(response.results.error.message)
            } else {
                if (response.results.shop.isEmpty()) {
                    ApiResult.Error("Not Found")
                } else {
                    ApiResult.Success(response.results.shop.get(0))
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}