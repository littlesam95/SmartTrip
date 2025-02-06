package com.neungi.moyeo.views.plan.tripviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Trip
import com.neungi.domain.usecase.GetTripUseCase
import com.neungi.moyeo.util.MarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val getTripUseCase: GetTripUseCase,

): ViewModel(){
    private val BASE_URL = "http://43.202.51.112:8080/"

    private val _tripUiState = MutableStateFlow<TripUiState>(TripUiState())
    val tripUiState = _tripUiState.asStateFlow()

    private val _tripUiEvent = MutableSharedFlow<TripUiEvent>()
    val tripUiEvent = _tripUiEvent.asSharedFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips = _trips.asStateFlow()

    init {
//        val newTrips = mutableListOf<TripData>()
//        newTrips.add(TripData(0,"제주도","1231-111","3명"))
//        newTrips.add(TripData(1,"강원","1231-111","4명"))
//        newTrips.add(TripData(2,"서울","1231-111","2명"))
//        newTrips.add(TripData(3,"충청","1231-111","5명"))
//        newTrips.add(TripData(4,"평창","1231-111","3명"))
//        _trips.value = newTrips.toList()

        viewModelScope.launch {
            val result = getTripUseCase.getTrips(1)
            Timber.d(result.toString())
            when (result.status) {
                ApiStatus.SUCCESS -> {
                    // result.data가 null이 아니고 List<Trip>으로 캐스팅 가능한지 확인
                    val tripList = result.data as? List<Trip>
                    if (tripList != null) {
                        _trips.value = result.data!!
                    } else {
                        _trips.value = emptyList() // data가 null이거나 List<Trip>이 아닌 경우 처리
                    }
                }
                ApiStatus.ERROR -> {
                    // ApiResult.Status가 ERROR일 때 처리
                    // 예: UI 상태 변경 또는 에러 메시지 표시
                    // 예시: _tripUiState.value = TripUiState(errorMessage = result.message)
                }
                ApiStatus.FAIL -> {
                    // ApiResult.Status가 FAIL일 때 처리
                    // 예시: _tripUiState.value = TripUiState(failed = true)
                }
                ApiStatus.LOADING -> {
                    // ApiResult.Status가 LOADING일 때 처리
                    // 예시: _tripUiState.value = TripUiState(loading = true)
                }
            }
        }
    }
}