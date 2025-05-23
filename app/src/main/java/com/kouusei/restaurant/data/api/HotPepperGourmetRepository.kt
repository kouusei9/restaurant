package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.Address
import com.kouusei.restaurant.data.api.entities.Results
import com.kouusei.restaurant.data.api.entities.Shop
import com.kouusei.restaurant.data.api.entities.ShopName
import com.kouusei.restaurant.data.utils.ApiResult

interface HotPepperGourmetRepository {
    /**
     * 1. keyword: keyword search
     * 2. lat + lng + range
     * 3. start from page
     * 4. filters 0 なし 1 あり
     */
    suspend fun searchShops(
        keyword: String?,
        lat: Double?,
        lng: Double?,
        range: Int?,
        start: Int = 1,
        order: Int?,
        genre: String?,
        largeArea: String?,
        middleArea: String?,
        smallArea: String?,
        filters: Map<String, String>
    ): ApiResult<Results>

    /**
     * search shop by names
     */
    suspend fun searchShopNames(
        keyword: String
    ): ApiResult<List<ShopName>>

    /**
     * search shop by id
     */
    suspend fun shopDetailById(id: String): ApiResult<Shop>

    /**
     * in: set of shop ids
     * out: list of shops detail.
     */
    suspend fun getShopsByIds(ids: List<String>): ApiResult<List<Shop>>

    suspend fun getLargeArea(): ApiResult<List<Address>>

    suspend fun getMiddleArea(largeArea: String): ApiResult<List<Address>>

    suspend fun getSmallArea(middleArea: String): ApiResult<List<Address>>

}