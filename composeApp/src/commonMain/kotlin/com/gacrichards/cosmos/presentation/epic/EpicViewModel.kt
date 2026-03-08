package com.gacrichards.cosmos.presentation.epic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.usecase.GetEpicAvailableDatesUseCase
import com.gacrichards.cosmos.domain.usecase.GetEpicImagesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EpicViewModel(
    private val getImages: GetEpicImagesUseCase,
    private val getAvailableDates: GetEpicAvailableDatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpicUiState())
    val uiState: StateFlow<EpicUiState> = _uiState.asStateFlow()

    private var imagesJob: Job? = null

    init {
        loadAvailableDates()
    }

    fun onEvent(event: EpicEvent) {
        when (event) {
            is EpicEvent.NextDate -> {
                val state = _uiState.value
                if (state.canGoNext) {
                    _uiState.update { it.copy(currentDateIndex = it.currentDateIndex - 1, images = UiState.Loading) }
                    loadImages()
                }
            }
            is EpicEvent.PreviousDate -> {
                val state = _uiState.value
                if (state.canGoPrevious) {
                    _uiState.update { it.copy(currentDateIndex = it.currentDateIndex + 1, images = UiState.Loading) }
                    loadImages()
                }
            }
            is EpicEvent.Retry -> {
                _uiState.update { it.copy(images = UiState.Loading) }
                loadImages()
            }
        }
    }

    private fun loadAvailableDates() {
        viewModelScope.launch {
            getAvailableDates().collect { result ->
                result.fold(
                    onSuccess = { dates ->
                        _uiState.update { it.copy(availableDates = dates, currentDateIndex = 0) }
                        loadImages()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(images = UiState.Error(e.message ?: "Failed to load dates")) }
                    },
                )
            }
        }
    }

    private fun loadImages() {
        val date = _uiState.value.currentDate ?: return
        imagesJob?.cancel()
        imagesJob = viewModelScope.launch {
            getImages(date).collect { result ->
                result.fold(
                    onSuccess = { images ->
                        _uiState.update { it.copy(images = UiState.Success(images)) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(images = UiState.Error(e.message ?: "Failed to load images")) }
                    },
                )
            }
        }
    }
}
