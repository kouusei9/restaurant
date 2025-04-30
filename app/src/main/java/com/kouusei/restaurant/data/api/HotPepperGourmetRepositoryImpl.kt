package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.Address
import com.kouusei.restaurant.data.api.entities.Results
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.data.api.entities.ShopName
import com.kouusei.restaurant.data.api.entities.toShopName
import com.kouusei.restaurant.data.utils.ApiResult
import kotlinx.coroutines.delay
import javax.inject.Inject

const val TAG = "HotPepperGourmetRepositoryImpl"

class HotPepperGourmetRepositoryImpl @Inject constructor(private val apiService: HotPepperGourmetService) :
    HotPepperGourmetRepository {

    companion object ErrorCode {
        // internal error
        const val INTERNALERROR: Int = 100

        // Not Found
        const val NOTFOUNDERROR: Int = 404
    }

    override suspend fun searchShops(
        keyword: String?,
        lat: Double?,
        lng: Double?,
        range: Int?,
        start: Int,
        order: Int?,
        genre: String?,
        largeArea: String?,
        middleArea: String?,
        smallArea: String?,
        filters: Map<String, String>
    ): ApiResult<Results> {
        delay(300)
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
                    genre = genre,
                    largeArea = largeArea,
                    middleArea = middleArea,
                    smallArea = smallArea
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
            ApiResult.Error(code = INTERNALERROR, message = e.message ?: "Unknown error")
        }
    }

    override suspend fun searchShopNames(keyword: String): ApiResult<List<ShopName>> {
        return try {
            val response = apiService.gourmetSearch(keyword = keyword)
            if (response.results.error != null) {
                ApiResult.Error(
                    code = response.results.error.code,
                    message = response.results.error.message
                )
            } else {
                ApiResult.Success(response.results.shop.map { it.toShopName() })
            }
        } catch (e: Exception) {
            ApiResult.Error(code = INTERNALERROR, e.message ?: "Unknown error")
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
                    ApiResult.Error(code = NOTFOUNDERROR, message = "Not Found")
                } else {
                    ApiResult.Success(response.results.shop[0])
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(code = INTERNALERROR, e.message ?: "Unknown error")
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
                    ApiResult.Error(code = NOTFOUNDERROR, message = "Not Found")
                } else {
                    ApiResult.Success(response.results.shop)
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(code = INTERNALERROR, e.message ?: "Unknown error")
        }
    }

    override suspend fun getLargeArea(): ApiResult<List<Address>> {
        return try {
            val response = apiService.largeAreaSearch()
            if (response.results.results_available == 0) {
                ApiResult.Error(code = NOTFOUNDERROR, message = "Not Found")
            } else {
                ApiResult.Success(response.results.large_area)
            }
        } catch (e: Exception) {
            ApiResult.Error(code = INTERNALERROR, e.message ?: "Unknown error")
        }
    }

    override suspend fun getMiddleArea(largeArea: String): ApiResult<List<Address>> {
        return try {
            val response = apiService.middleAreaSearch(largeArea = largeArea)
            if (response.results.results_available == 0) {
                ApiResult.Error(code = NOTFOUNDERROR, message = "Not Found")
            } else {
                ApiResult.Success(response.results.middle_area)
            }
        } catch (e: Exception) {
            ApiResult.Error(code = INTERNALERROR, e.message ?: "Unknown error")
        }
    }

    override suspend fun getSmallArea(middleArea: String): ApiResult<List<Address>> {
        return try {
            val response = apiService.smallAreaSearch(middleArea = middleArea)
            if (response.results.results_available == 0) {
                ApiResult.Error(code = NOTFOUNDERROR, message = "Not Found")
            } else {
                ApiResult.Success(response.results.small_area)
            }
        } catch (e: Exception) {
            ApiResult.Error(code = INTERNALERROR, e.message ?: "Unknown error")
        }
    }
}