package com.kouusei.restaurant.presentation.favoriteview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.local.FavoriteShopRepository
import com.kouusei.restaurant.data.utils.ApiResult
import com.kouusei.restaurant.presentation.mappers.toShopSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FavoriteShopsModel @Inject constructor(
    val favoriteShopRepository: FavoriteShopRepository,
    val hotPepperGourmetRepository: HotPepperGourmetRepository
) : ViewModel() {

    private var _shopIds = MutableStateFlow<Set<String>>(emptySet())
    val shopIds = _shopIds.asStateFlow()

    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState.Loading)
    val favoriteState = _favoriteState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteShopRepository.getAllFavoriteShopIds().stateIn(
                viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet()
            ).collect {
                _shopIds.value = it
                loadShops(it)
            }
        }
    }

    fun toggleFavorite(shopId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                favoriteShopRepository.toggleFavorite(shopId)
            }
        }
    }

    private suspend fun loadShops(ids: Set<String>) {
        if (ids.isEmpty()) {
            _favoriteState.value = FavoriteState.Empty
            return
        }
        val result = hotPepperGourmetRepository.getShopsByIds(ids)
        when (result) {
            is ApiResult.Error -> {
                _favoriteState.value = FavoriteState.Error(result.message)
            }

            is ApiResult.Success -> {
                if (result.data.isEmpty()) {
                    _favoriteState.value = FavoriteState.Empty
                } else {
                    _favoriteState.value =
                        FavoriteState.Success(result.data.map { it.toShopSummary() })
                }
            }
        }
    }
}