package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.entities.ShopNameResponse
import com.kouusei.restaurant.data.api.entities.ShopResponse
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
}