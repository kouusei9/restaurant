package com.kouusei.restaurant.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteShopRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : FavoriteShopRepository {
    object Keys {
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_ids")
    }

    private val favoriteShopsFlow: Flow<Set<String>> = dataStore.data
        .map { prefs -> prefs[Keys.FAVORITE_IDS] ?: emptySet() }

    override suspend fun toggleFavorite(shopId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_IDS] ?: emptySet()
            prefs[Keys.FAVORITE_IDS] = if (shopId in current) current - shopId else current + shopId
        }
    }

    override fun getAllFavoriteShopIds(): Flow<Set<String>> {
        return favoriteShopsFlow
    }
}