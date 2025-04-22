package com.kouusei.restaurant.data.api.di

import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.api.HotPepperGourmetRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class HotPepperGourmetRepositoryModule {
    @Binds
    abstract fun bindHotPepperGourmetRepository(hotPepperGourmetRepositoryImpl: HotPepperGourmetRepositoryImpl): HotPepperGourmetRepository
}