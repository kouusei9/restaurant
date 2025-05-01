package com.kouusei.restaurant.presentation.favoriteview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.api.entities.Shop
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

const val TAG = "FavoriteShopsModel"

@HiltViewModel
class FavoriteShopsModel @Inject constructor(
    val favoriteShopRepository: FavoriteShopRepository,
    val hotPepperGourmetRepository: HotPepperGourmetRepository
) : ViewModel() {

    private var _shopIds = MutableStateFlow<List<FavoriteShop>>(emptyList())
    val shopIds = _shopIds.asStateFlow()

    private val _shops = MutableStateFlow<List<FavoriteShopSummary>>(emptyList())
    private val perPage = 10

    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState.Loading)
    val favoriteState = _favoriteState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isReachEnd = MutableStateFlow(false)
    val isReachEnd: StateFlow<Boolean> = _isReachEnd.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

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
                if (isAscending.value) {
                    _shopIds.value = it.sortedBy { it.timestamp }
                } else {
                    _shopIds.value = it.sortedByDescending { it.timestamp }
                }
                Log.d(TAG, ": _shopIds.value = $it")
                reload()
            }
        }
    }

    fun onLoadMore() {
        _isLoadingMore.value = true
        viewModelScope.launch {
            val ids = _shopIds.value.map { it.shopId }
                .drop(_shops.value.size)
                .take(perPage)
                .toList()
            if (ids.isEmpty()) {
                _isReachEnd.value = true
                _isLoadingMore.value = false
                return@launch
            }
            loadShops(ids, ::onAddMore)
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

    fun toggleFavorite(shopId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                favoriteShopRepository.toggleFavorite(shopId)
            }
        }
    }

    fun toggleOrder() {
        _isAscending.value = !_isAscending.value
        if (isAscending.value) {
            _shopIds.value = _shopIds.value.sortedBy { it.timestamp }
        } else {
            _shopIds.value = _shopIds.value.sortedByDescending { it.timestamp }
        }
        reload()
    }

    private fun onReload(result: ApiResult<List<Shop>>) {
        _isLoading.value = false
        when (result) {
            is ApiResult.Error -> {
                _favoriteState.value = FavoriteState.Error(result.message)
            }

            is ApiResult.Success -> {
                if (result.data.isEmpty()) {
                    _shops.value = emptyList()
                    _favoriteState.value = FavoriteState.Empty
                } else {
                    val favoriteShops = mutableListOf<FavoriteShopSummary>()
                    favoriteShops.addAll(result.data.map { it.toShopSummary() }
                        .map { shopSummary ->
                            FavoriteShopSummary(
                                shopSummary,
                                _shopIds.value.find { it.shopId == shopSummary.id }?.timestamp
                                    ?: System.currentTimeMillis()
                            )
                        })
                    if (isAscending.value) {
                        favoriteShops.sortBy { it.timestamp }
                    } else {
                        favoriteShops.sortByDescending { it.timestamp }
                    }
                    _favoriteState.value =
                        FavoriteState.Success(favoriteShops)

                    _shops.value = favoriteShops
                    toggleReachEnd()
                }
            }
        }
    }

    private fun toggleReachEnd() {
        Log.d(
            TAG,
            "toggleReachEnd: _shops.value.size = ${_shops.value.size}, _shopIds.value.size = ${_shopIds.value.size}"
        )
        _isReachEnd.value = _shops.value.size >= _shopIds.value.size
    }

    private fun onAddMore(result: ApiResult<List<Shop>>) {
        _isLoadingMore.value = false
        when (result) {
            is ApiResult.Error -> {
                _favoriteState.value = FavoriteState.Error(result.message)
            }

            is ApiResult.Success -> {
                val shops = mutableListOf<FavoriteShopSummary>()
                shops.addAll(_shops.value)
                val favoriteShops = result.data.map { it.toShopSummary() }
                    .map { shopSummary ->
                        FavoriteShopSummary(
                            shopSummary,
                            _shopIds.value.find { it.shopId == shopSummary.id }?.timestamp
                                ?: System.currentTimeMillis()
                        )
                    }
                shops.addAll(favoriteShops)
                if (isAscending.value) {
                    shops.sortBy { it.timestamp }
                } else {
                    shops.sortByDescending { it.timestamp }
                }
                _shops.value = shops
                _favoriteState.value = FavoriteState.Success(shops)
                toggleReachEnd()
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            if (_shopIds.value.isEmpty()) {
                _shops.value = emptyList()
                _favoriteState.value = FavoriteState.Empty
                return@launch
            }
            _isLoading.value = true
            _isReachEnd.value = false
            loadShops(_shopIds.value.map { it.shopId }.take(perPage).toList(), ::onReload)
        }
    }


    private suspend fun loadShops(ids: List<String>, onResult: (ApiResult<List<Shop>>) -> Unit) {
        val result = hotPepperGourmetRepository.getShopsByIds(ids)
        onResult(result)
    }
}