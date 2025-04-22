package com.kouusei.restaurant.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.utils.ApiResult
import com.kouusei.restaurant.presentation.mappers.toShopSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val gourmetRepository: HotPepperGourmetRepository
) : ViewModel() {
    private val _restaurantViewStateFlow =
        MutableStateFlow<RestaurantViewState>(RestaurantViewState.Loading)
    val restaurantViewState: StateFlow<RestaurantViewState> = _restaurantViewStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = gourmetRepository.searchShops(
                    keyword = "",
                    lat = 34.67,
                    lng = 135.52,
                )
                when (result) {
                    is ApiResult.Error -> {
                        _restaurantViewStateFlow.value = RestaurantViewState.Error(result.message)
                    }

                    is ApiResult.Success -> {
                        _restaurantViewStateFlow.value =
                            RestaurantViewState.Success(result.data.map { it.toShopSummary() })
                    }
                }
            }
        }
    }
}


