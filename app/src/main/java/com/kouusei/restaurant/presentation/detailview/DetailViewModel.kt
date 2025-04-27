package com.kouusei.restaurant.presentation.detailview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kouusei.restaurant.data.api.HotPepperGourmetRepository
import com.kouusei.restaurant.data.utils.ApiResult
import com.kouusei.restaurant.presentation.mappers.toShopDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val hotPepperGourmetRepository: HotPepperGourmetRepository
) : ViewModel() {

    private val _detailViewState = MutableStateFlow<DetailViewState>(DetailViewState.Loading)
    val detailViewState: StateFlow<DetailViewState> = _detailViewState.asStateFlow()

    private val _title = MutableStateFlow<String>("")
    val title: StateFlow<String> = _title.asStateFlow()

    init {

    }

    fun load(id: String) {
        _title.value = ""
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = hotPepperGourmetRepository.shopDetailById(id)
                when (result) {
                    is ApiResult.Error -> {
                        _detailViewState.value = DetailViewState.Error(result.message)
                    }

                    is ApiResult.Success -> {
                        _detailViewState.value = DetailViewState.Success(result.data.toShopDetail())
                        _title.value = result.data.name
                    }
                }
            }
        }
    }
}