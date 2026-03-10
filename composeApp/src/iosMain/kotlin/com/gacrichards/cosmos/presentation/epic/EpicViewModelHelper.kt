package com.gacrichards.cosmos.presentation.epic

import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.EpicImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class EpicViewModelHelper : KoinComponent {
    private val viewModel: EpicViewModel = get()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null
    private var epicImages: List<EpicImage> = emptyList()

    val imageCount: Int get() = epicImages.size
    fun imageAt(index: Int): EpicImage? = epicImages.getOrNull(index)

    var currentDate: String = ""
        private set
    var canGoPrevious: Boolean = false
        private set
    var canGoNext: Boolean = false
        private set

    fun startObserving(
        onLoading: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        job = scope.launch {
            viewModel.uiState.collect { epicState ->
                when (val images = epicState.images) {
                    is UiState.Loading -> onLoading()
                    is UiState.Success -> {
                        epicImages = images.data
                        currentDate = epicState.currentDate ?: ""
                        canGoPrevious = epicState.canGoPrevious
                        canGoNext = epicState.canGoNext
                        onSuccess()
                    }
                    is UiState.Error -> onError(images.message)
                }
            }
        }
    }

    fun nextDate() = viewModel.onEvent(EpicEvent.NextDate)
    fun previousDate() = viewModel.onEvent(EpicEvent.PreviousDate)
    fun retry() = viewModel.onEvent(EpicEvent.Retry)
    fun dispose() { job?.cancel() }
}
