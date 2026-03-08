package com.gacrichards.cosmos.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.usecase.GetTodayApodUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TodayViewModel(
    private val getTodayApod: GetTodayApodUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Apod>>(UiState.Loading)
    val uiState: StateFlow<UiState<Apod>> = _uiState.asStateFlow()

    init {
        loadToday()
    }

    fun onEvent(event: TodayEvent) {
        when (event) {
            TodayEvent.Retry -> loadToday()
        }
    }

    private fun loadToday() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getTodayApod()
                .catch { _uiState.value = UiState.Error(it.message ?: "Unknown error") }
                .collect { result ->
                    _uiState.value = result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "Unknown error") },
                    )
                }
        }
    }
}
