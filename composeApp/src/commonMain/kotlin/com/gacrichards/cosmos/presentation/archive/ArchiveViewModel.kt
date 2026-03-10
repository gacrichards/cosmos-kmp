package com.gacrichards.cosmos.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.usecase.GetApodArchiveUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class ArchiveViewModel(
    private val getArchive: GetApodArchiveUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    // Load the most recent 30 days on init
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val startDate = today.minus(29, DateTimeUnit.DAY).toString()
    private val endDate = today.toString()

    init {
        load()
    }

    fun onEvent(event: ArchiveEvent) {
        when (event) {
            is ArchiveEvent.Retry -> {
                _uiState.update { it.copy(apods = UiState.Loading) }
                load()
            }
            is ArchiveEvent.LoadMore -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            getArchive(startDate, endDate).collect { result ->
                result.fold(
                    onSuccess = { apods ->
                        _uiState.update { it.copy(apods = UiState.Success(apods)) }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(apods = UiState.Error(e.message ?: "Failed to load archive"))
                        }
                    },
                )
            }
        }
    }
}
