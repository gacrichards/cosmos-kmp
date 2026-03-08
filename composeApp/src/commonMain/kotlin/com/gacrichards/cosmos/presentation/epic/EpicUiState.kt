package com.gacrichards.cosmos.presentation.epic

import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.EpicImage

data class EpicUiState(
    val availableDates: List<String> = emptyList(),
    val currentDateIndex: Int = 0,
    val images: UiState<List<EpicImage>> = UiState.Loading,
) {
    val currentDate: String? get() = availableDates.getOrNull(currentDateIndex)
    val canGoPrevious: Boolean get() = currentDateIndex < availableDates.size - 1
    val canGoNext: Boolean get() = currentDateIndex > 0
}
