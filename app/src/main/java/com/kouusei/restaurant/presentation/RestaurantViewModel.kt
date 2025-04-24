package com.kouusei.restaurant.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.api.entities.Results
import com.kouusei.restaurant.data.utils.ApiResult
import com.kouusei.restaurant.presentation.common.DistanceRange
import com.kouusei.restaurant.presentation.common.Filter
import com.kouusei.restaurant.presentation.entities.SearchFilters
import com.kouusei.restaurant.presentation.entities.ShopSummary
import com.kouusei.restaurant.presentation.mappers.toLatLngBounds
import com.kouusei.restaurant.presentation.mappers.toShopSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.text.isNotEmpty

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val gourmetRepository: HotPepperGourmetRepository
) : ViewModel() {
    val TAG = "RestaurantViewModel"

    private val _restaurantViewStateFlow =
        MutableStateFlow<RestaurantViewState>(RestaurantViewState.RequestPermission)
    val restaurantViewState: StateFlow<RestaurantViewState> = _restaurantViewStateFlow.asStateFlow()

    private val _shopNames = MutableStateFlow<List<String>>(emptyList())
    val shopNames = _shopNames.asStateFlow()

    private var _location: LatLng? = null

    private val _distanceRange = MutableStateFlow<DistanceRange>(DistanceRange.RANGE_1000M)
    val distanceRange: StateFlow<DistanceRange> = _distanceRange.asStateFlow()

    private val _keyword = MutableStateFlow<String>("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _searchFilters = MutableStateFlow<SearchFilters>(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _isReachEnd = MutableStateFlow<Boolean>(false)
    val isReachEnd: StateFlow<Boolean> = _isReachEnd.asStateFlow()

    init {

    }

    fun resetFilters() {
        _searchFilters.value = SearchFilters()
    }

    fun onKeyWordChange(keyword: String) {
        _keyword.value = keyword
    }

    fun toggleFilter(filter: Filter) {
        _searchFilters.value =
            when (filter) {
                Filter.Filter_FreeDrink -> _searchFilters.value.copy(
                    free_drink = !searchFilters.value.free_drink
                )

                Filter.Filter_FreeFood -> _searchFilters.value.copy(
                    free_food = !searchFilters.value.free_food
                )

                Filter.Filter_PrivateRoom -> _searchFilters.value.copy(
                    private_room = !searchFilters.value.private_room
                )
            }
        reloadShopList()
    }

    /**
     * used when call from top bar.
     * reset filters and range to no selected
     */
    fun searchShopsByName() {
        resetFilters()
        _distanceRange.value = DistanceRange.RANGE_NO
        reloadShopList()
    }

    fun reloadShopList() {
        // reset is reach end.
        _isReachEnd.value = false
        _isLoading.value = false

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadShopListByKeywordAndLocation(
                    keyword.value,
                    _location?.latitude,
                    _location?.longitude,
                    distanceRange.value
                ) { result ->
                    refreshShopList(result)
                }
            }
        }
    }

    /**
     * refresh shop list
     */
    private fun refreshShopList(result: ApiResult<Results>) {
        when (result) {
            is ApiResult.Error -> {
                _restaurantViewStateFlow.value =
                    RestaurantViewState.Error(result.message)
            }

            is ApiResult.Success -> {
                val shopList = result.data.shop.map { it.toShopSummary() }
                val boundingBox =
                    if (shopList.isEmpty()) listOf<LatLng>(_location!!).toLatLngBounds() else
                        result.data.shop.map { it.toShopSummary().location }.toLatLngBounds()
                _restaurantViewStateFlow.value =
                    RestaurantViewState.Success(
                        shopList = shopList,
                        boundingBox = boundingBox,
                        result.data.results_available
                    )
            }
        }
    }

    /**
     * append new shops to current shops
     */
    private fun appendShopList(result: ApiResult<Results>) {
        when (result) {
            is ApiResult.Error -> {
                _restaurantViewStateFlow.value =
                    RestaurantViewState.Error(result.message)
            }

            is ApiResult.Success -> {
                // in case of order, should add original list first.
                val combinedList: MutableList<ShopSummary> = mutableListOf()
                if (restaurantViewState.value is RestaurantViewState.Success) {
                    combinedList.addAll((restaurantViewState.value as RestaurantViewState.Success).shopList)
                }
                val shopList = result.data.shop.map { it.toShopSummary() }
                combinedList.addAll(shopList)
                val boundingBox =
                    if (combinedList.isEmpty()) listOf<LatLng>(_location!!).toLatLngBounds() else
                        combinedList.map { it.location }.toLatLngBounds()
                _restaurantViewStateFlow.value =
                    RestaurantViewState.Success(
                        shopList = combinedList,
                        boundingBox = boundingBox,
                        result.data.results_available
                    )
            }
        }
    }

    fun onDistanceRangeChange(distanceRange: DistanceRange) {
        _distanceRange.value = distanceRange
        reloadShopList()
    }

    /**
     * load shop name list
     */
    fun loadShopNameList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (keyword.value.isNotEmpty()) {
                    val result = gourmetRepository.searchShopNames(
                        keyword = keyword.value
                    )
                    when (result) {
                        is ApiResult.Error -> {
                            Log.d(TAG, "loadShopNameList: ${result.message}")
                        }

                        is ApiResult.Success -> {
                            _shopNames.value = result.data.map { it.name }
                        }
                    }
                } else {
                    _shopNames.value = emptyList<String>()
                }
            }
        }
    }

    /**
     * 1. keyword is not empty
     * search shop from only with keyword
     * 2. keyword is empty, range is not selected
     * set range to default 1000m, and search
     * 3. keyword is empty, range is selected
     * search with range
     */
    suspend fun loadShopListByKeywordAndLocation(
        keyword: String,
        lat: Double?,
        lng: Double?,
        range: DistanceRange,
        start: Int = 1,
        onResult: (ApiResult<Results>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            if (keyword.isNotEmpty()) {
                onResult(
                    gourmetRepository.searchShops(
                        keyword = keyword,
                        lat = null,
                        lng = null,
                        range = null,
                        filters = searchFilters.value.toQueryMap(),
                        start = start
                    )
                )
            } else if (range == DistanceRange.RANGE_NO) {
                _distanceRange.value = DistanceRange.RANGE_1000M
                onResult(
                    gourmetRepository.searchShops(
                        keyword = keyword,
                        lat = lat,
                        lng = lng,
                        range = range.value,
                        filters = searchFilters.value.toQueryMap(),
                        start = start
                    )
                )
            } else {
                onResult(
                    gourmetRepository.searchShops(
                        keyword = keyword,
                        lat = lat,
                        lng = lng,
                        range = range.value,
                        filters = searchFilters.value.toQueryMap(),
                        start = start
                    )
                )
            }
        }
    }

    fun loadMore() {
        if (restaurantViewState.value is RestaurantViewState.Success) {
            val state = restaurantViewState.value as RestaurantViewState.Success
            if (state.shopList.size >= state.totalSize) {
                _isReachEnd.value = true
                Log.d(TAG, "loadMore: isReachEnd to true: $isReachEnd")
                return
            }
        }
        _isLoading.value = true

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val start =
                    if (restaurantViewState.value is RestaurantViewState.Success)
                        (restaurantViewState.value as RestaurantViewState.Success).shopList.size + 1 else 1
                loadShopListByKeywordAndLocation(
                    keyword.value,
                    _location?.latitude,
                    _location?.longitude,
                    distanceRange.value,
                    start = start
                ) {
                    _isLoading.value = false
                    appendShopList(it)
                }
            }
        }
    }

    fun permissionSuccess(location: LatLng) {
        _restaurantViewStateFlow.value = RestaurantViewState.Loading
        _location = location
        Log.d(TAG, "permissionSuccess: location: $_location")
        viewModelScope.launch {
            reloadShopList()
        }
    }

    fun errMessage(message: String) {
        _restaurantViewStateFlow.value = RestaurantViewState.Error(message)
    }
}


