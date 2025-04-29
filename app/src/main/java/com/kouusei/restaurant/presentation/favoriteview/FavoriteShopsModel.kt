package com.kouusei.restaurant.presentation.favoriteview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.local.FavoriteShop
import com.kouusei.restaurant.data.local.FavoriteShopRepository
import com.kouusei.restaurant.data.utils.ApiResult
import com.kouusei.restaurant.presentation.mappers.toShopSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    private var _shopIds = MutableStateFlow<Set<FavoriteShop>>(emptySet())

    private val _shops = MutableStateFlow<List<FavoriteShopSummary>>(emptyList())

    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState.Loading)
    val favoriteState = _favoriteState.asStateFlow()

    // filter keyword
    private val _keyword = MutableStateFlow<String>("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()
    fun onKeyWordChange(keyword: String) {
        _keyword.value = keyword
    }

    init {
        viewModelScope.launch {
            favoriteShopRepository.getAllFavoriteShops().stateIn(
                viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet()
            ).collect {
                _shopIds.value = it
                loadShops(it.map { it.shopId }.toSet())
            }
        }
    }

    fun filter() {
        if (favoriteState.value is FavoriteState.Success) {
            val filterShops =
                _shops.value.filter { it.shopSummary.name.contains(keyword.value) }
            _favoriteState.value = FavoriteState.Success(filterShops)
        }
    }

    fun getSuggestionsByKeyword(): List<String> {
        return _shops.value.map { it.shopSummary.name }.filter { it.contains(keyword.value) }
    }

    fun isFavorite(shopId: String): Boolean {
        return _shopIds.value.find { it.shopId == shopId } != null
    }

    fun toggleFavorite(shopId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                favoriteShopRepository.toggleFavorite(shopId)
            }
        }
    }

    fun toggleOrder() {
        if (favoriteState.value is FavoriteState.Success) {
            _favoriteState.value =
                FavoriteState.Success((favoriteState.value as FavoriteState.Success).shops.reversed())
        }

    }

    private suspend fun loadShops(ids: Set<String>) {
        if (ids.isEmpty()) {
            _shops.value = emptyList()
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
                    _shops.value = emptyList()
                    _favoriteState.value = FavoriteState.Empty
                } else {
                    val favoriteShops = result.data.map { it.toShopSummary() }
                        .map { shopSummary ->
                            FavoriteShopSummary(
                                shopSummary,
                                _shopIds.value.find { it.shopId == shopSummary.id }?.timestamp
                                    ?: System.currentTimeMillis()
                            )
                        }.sortedBy { it.timestamp }
                    _favoriteState.value =
                        FavoriteState.Success(favoriteShops)

                    _shops.value = favoriteShops
                }
            }
        }
    }
}