package com.kouusei.restaurant.data.local.di

import com.kouusei.restaurant.data.local.FavoriteShopRepository
import com.kouusei.restaurant.data.local.FavoriteShopRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoriteShopModule {
    @Binds
    abstract fun bindFavoriteShop(favoriteShopRepositoryImpl: FavoriteShopRepositoryImpl): FavoriteShopRepository
}