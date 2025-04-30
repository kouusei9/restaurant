package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.LargeAreaResponse
import com.kouusei.restaurant.data.api.entities.MiddleAreaResponse
import com.kouusei.restaurant.data.api.entities.ShopNameResponse
import com.kouusei.restaurant.data.api.entities.ShopResponse
import com.kouusei.restaurant.data.api.entities.SmallAreaResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface HotPepperGourmetService {
    @GET("gourmet/v1/")
    suspend fun gourmetSearch(
        @Query("key") apiKey: String = "471c0bda908a2b37",
        @Query("format") format: String = "json",
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("keyword") keyword: String? = null,
        @Query("range") range: Int? = 3,
        @Query("count") count: Int = 10,
        @Query("order") order: Int? = 4,
        @Query("start") start: Int = 1,
        @Query("id") id: String? = null,
        @Query("genre") genre: String? = null,
        @Query("large_area") largeArea: String? = null,
        @Query("middle_area") middleArea: String? = null,
        @Query("small_area") smallArea: String? = null,
        @QueryMap filters: Map<String, String> = emptyMap<String, String>()
    ): ShopResponse

    @GET("shop/v1/")
    suspend fun shopNameSearch(
        @Query("key") apiKey: String = "471c0bda908a2b37",
        @Query("format") format: String = "json",
        @Query("keyword") keyword: String = "",
        @Query("start") start: Int = 1,
        @Query("count") count: Int = 10,
    ): ShopNameResponse

    @GET("large_area/v1/")
    suspend fun largeAreaSearch(
        @Query("key") apiKey: String = "471c0bda908a2b37",
        @Query("format") format: String = "json"
    ): LargeAreaResponse

    @GET("middle_area/v1/")
    suspend fun middleAreaSearch(
        @Query("key") apiKey: String = "471c0bda908a2b37",
        @Query("format") format: String = "json",
        @Query("large_area") largeArea: String
    ): MiddleAreaResponse

    @GET("small_area/v1/")
    suspend fun smallAreaSearch(
        @Query("key") apiKey: String = "471c0bda908a2b37",
        @Query("format") format: String = "json",
        @Query("middle_area") middleArea: String,
    ): SmallAreaResponse
}