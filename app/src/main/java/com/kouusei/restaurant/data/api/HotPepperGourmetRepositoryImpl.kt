package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.Results
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.data.api.entities.ShopName
import com.kouusei.restaurant.data.utils.ApiResult
import javax.inject.Inject

val TAG = "HotPepperGourmetRepositoryImpl"

class HotPepperGourmetRepositoryImpl @Inject constructor(private val apiService: HotPepperGourmetService) :
    HotPepperGourmetRepository {

    companion object ErrorCode {
        // internal error
        const val internalError: Int = 100

        // Not Found
        const val notFoundError: Int = 404
    }

    override suspend fun searchShops(
        keyword: String?,
        lat: Double?,
        lng: Double?,
        range: Int?,
        start: Int,
        order: Int?,
        genre: String?,
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
                    order = order,
                    genre = genre
                )
            if (response.results.error != null) {
                ApiResult.Error(
                    code = response.results.error.code,
                    message = response.results.error.message
                )
            } else {
                ApiResult.Success(response.results)
            }
        } catch (e: Exception) {
            ApiResult.Error(code = internalError, message = e.message ?: "Unknown error")
        }
    }

    override suspend fun searchShopNames(keyword: String): ApiResult<List<ShopName>> {
        return try {
            val response = apiService.shopNameSearch(keyword = keyword)
            if (response.results.error != null) {
                ApiResult.Error(
                    code = response.results.error.code,
                    message = response.results.error.message
                )
            } else {
                ApiResult.Success(response.results.shop)
            }
        } catch (e: Exception) {
            ApiResult.Error(code = internalError, e.message ?: "Unknown error")
        }
    }

    override suspend fun shopDetailById(id: String): ApiResult<Shop> {
        return try {
            val response =
                apiService.gourmetSearch(id = id)
            if (response.results.error != null) {
                ApiResult.Error(
                    code = response.results.error.code,
                    message = response.results.error.message
                )
            } else {
                if (response.results.shop.isEmpty()) {
                    ApiResult.Error(code = notFoundError, message = "Not Found")
                } else {
                    ApiResult.Success(response.results.shop.get(0))
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(code = internalError, e.message ?: "Unknown error")
        }
    }

    override suspend fun getShopsByIds(ids: Set<String>): ApiResult<List<Shop>> {
        return try {
            val response =
                apiService.gourmetSearch(id = ids.joinToString(","))
            if (response.results.error != null) {
                ApiResult.Error(
                    code = response.results.error.code,
                    message = response.results.error.message
                )
            } else {
                if (response.results.shop.isEmpty()) {
                    ApiResult.Error(code = notFoundError, message = "Not Found")
                } else {
                    ApiResult.Success(response.results.shop)
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(code = internalError, e.message ?: "Unknown error")
        }
    }
}