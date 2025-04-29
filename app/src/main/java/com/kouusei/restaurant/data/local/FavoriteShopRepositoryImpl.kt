package com.kouusei.restaurant.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class FavoriteShopRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : FavoriteShopRepository {
    object Keys {
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_ids")
    }

    private val json = Json

    private val favoriteShopsFlow: Flow<Set<FavoriteShop>> = dataStore.data
        .map { prefs ->
            prefs[Keys.FAVORITE_IDS]?.mapNotNull {
                runCatching { json.decodeFromString<FavoriteShop>(it) }.getOrNull()
            }?.toSet() ?: emptySet()
        }

    override suspend fun toggleFavorite(shopId: String) {
        dataStore.edit { prefs ->
            val current: MutableSet<FavoriteShop> = prefs[Keys.FAVORITE_IDS]
                ?.mapNotNull { runCatching { json.decodeFromString<FavoriteShop>(it) }.getOrNull() }
                ?.toMutableSet()
                ?: mutableSetOf()

            val existing = current.find { it.shopId == shopId }
            if (existing != null) {
                current.remove(existing)
            } else {
                current.add(FavoriteShop(shopId = shopId, timestamp = System.currentTimeMillis()))
            }

            prefs[Keys.FAVORITE_IDS] = current.map { json.encodeToString(it) }.toSet()
        }
    }

    override fun getAllFavoriteShops(): Flow<Set<FavoriteShop>> {
        return favoriteShopsFlow
    }
}