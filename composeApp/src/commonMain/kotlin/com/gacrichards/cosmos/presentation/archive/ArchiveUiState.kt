package com.gacrichards.cosmos.presentation.archive

import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod

data class ArchiveUiState(
    val apods: UiState<List<Apod>> = UiState.Loading,
)
