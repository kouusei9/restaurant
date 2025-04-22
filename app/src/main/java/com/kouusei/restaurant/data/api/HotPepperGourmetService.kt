package com.kouusei.restaurant.data.api

import com.kouusei.restaurant.data.api.model.ShopResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HotPepperGourmetService {
    @GET("gourmet/v1/")
    suspend fun gourmetSearch(
        @Query("key") apiKey: String = "471c0bda908a2b37",
        @Query("format") format: String = "json",
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("keyword") keyword: String,
        @Query("range") range:Int = 5,
        @Query("count") count: Int = 10
    ): ShopResponse
}