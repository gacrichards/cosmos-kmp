package com.gacrichards.cosmos.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.usecase.GetApodByDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaDetailViewModel(
    private val getApodByDate: GetApodByDateUseCase,
    private val date: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Apod>>(UiState.Loading)
    val uiState: StateFlow<UiState<Apod>> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            getApodByDate(date).collect { result ->
                result.fold(
                    onSuccess = { apod -> _uiState.update { UiState.Success(apod) } },
                    onFailure = { e -> _uiState.update { UiState.Error(e.message ?: "Failed to load") } },
                )
            }
        }
    }
}
