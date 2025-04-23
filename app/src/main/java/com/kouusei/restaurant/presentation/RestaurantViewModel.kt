package com.kouusei.restaurant.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.utils.ApiResult
import com.kouusei.restaurant.presentation.common.DistanceRange
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

    init {

    }

    fun onKeyWordChange(keyword: String) {
        _keyword.value = keyword
    }

    fun reloadShopList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadShopListByKeywordAndLocation(
                    keyword.value,
                    _location?.latitude,
                    _location?.longitude,
                    distanceRange.value
                )
            }
        }
    }

    fun reloadShopListName() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
//                loadShopNameListByKeywordAndLocation(
//                    keyword.value,
//                    _location?.latitude,
//                    _location?.longitude,
//                    distanceRange.value
//                )
            }
        }
    }

    fun distanceRangeChange(distanceRange: DistanceRange) {
        _distanceRange.value = distanceRange
        reloadShopList()
    }

    suspend fun loadShopListByLocation(lat: Double, lng: Double, range: DistanceRange) {
        loadShopListByKeywordAndLocation("", lat, lng, range)
    }

    suspend fun loadShopNameList() {
        val result =
            if (keyword.value.isNotEmpty() && distanceRange.value == DistanceRange.RANGE_NO)
                gourmetRepository.searchShops(
                    keyword = keyword.value, null, null, null
                ) else gourmetRepository.searchShops(
                keyword = keyword.value,
                lat = _location?.latitude,
                lng = _location?.longitude,
                distanceRange.value.value
            )
        when (result) {
            is ApiResult.Error -> {
//                _restaurantViewStateFlow.value = RestaurantViewState.Error(result.message)
            }

            is ApiResult.Success -> {
                _shopNames.value = result.data.map { it.name }
            }
        }
    }

    suspend fun loadShopListByKeywordAndLocation(
        keyword: String,
        lat: Double?,
        lng: Double?,
        range: DistanceRange
    ) {
        withContext(Dispatchers.IO) {
            val result =
                if (keyword.isNotEmpty() && range == DistanceRange.RANGE_NO) gourmetRepository.searchShops(
                    keyword = keyword, null, null, null
                ) else gourmetRepository.searchShops(
                    keyword = keyword,
                    lat = lat,
                    lng = lng,
                    range.value
                )
            when (result) {
                is ApiResult.Error -> {
                    _restaurantViewStateFlow.value = RestaurantViewState.Error(result.message)
                }

                is ApiResult.Success -> {
                    val shopList = result.data.map { it.toShopSummary() }
                    val boundingBox =
                        if (shopList.isEmpty()) listOf<LatLng>(_location!!).toLatLngBounds() else
                            result.data.map { it.toShopSummary().location }.toLatLngBounds()
                    _restaurantViewStateFlow.value =
                        RestaurantViewState.Success(
                            shopList = shopList,
                            boundingBox = boundingBox
                        )
                }
            }
        }
    }

    fun loadShopNameListByKeyword(keyword: String) {
        if (keyword.isEmpty()) {
            _shopNames.value = emptyList<String>()
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = gourmetRepository.searchShopsByKeyword(keyword = keyword)
                when (result) {
                    is ApiResult.Error -> {
//                        _restaurantViewStateFlow.value = RestaurantViewState.Error(result.message)
                    }

                    is ApiResult.Success -> {
                        _shopNames.value = result.data.map { it.name }
                    }
                }
            }
        }
    }

    fun permissionSuccess(location: LatLng) {
        _restaurantViewStateFlow.value = RestaurantViewState.Loading
        _location = location
        Log.d(TAG, "permissionSuccess: location: $_location")
        viewModelScope.launch {
            // default
            loadDefault(location)
        }
    }

    suspend fun loadDefault(location: LatLng) {
        loadShopListByLocation(location.latitude, location.longitude, DistanceRange.RANGE_1000M)
    }

    fun errMessage(message: String) {
        _restaurantViewStateFlow.value = RestaurantViewState.Error(message)
    }
}


