package com.kouusei.restaurant.data.local

import kotlinx.coroutines.flow.Flow

interface FavoriteShopRepository {
    suspend fun toggleFavorite(shopId: String)

    fun getAllFavoriteShopIds(): Flow<Set<String>>
}