package com.gacrichards.cosmos.presentation.archive

import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ArchiveViewModelHelper : KoinComponent {

    private val viewModel: ArchiveViewModel = get()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null
    private var apods: List<Apod> = emptyList()

    // Expose as Int (maps to Int32 in Swift) for indexed access — avoids Swift collection bridging issues
    val apodCount: Int get() = apods.size
    fun apodAt(index: Int): Apod? = apods.getOrNull(index)

    fun startObserving(
        onLoading: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        job = scope.launch {
            viewModel.uiState.collect { archiveState ->
                when (val state = archiveState.apods) {
                    is UiState.Loading -> onLoading()
                    is UiState.Success -> {
                        apods = state.data
                        onSuccess()
                    }
                    is UiState.Error -> onError(state.message)
                }
            }
        }
    }

    fun retry() = viewModel.onEvent(ArchiveEvent.Retry)

    fun dispose() {
        job?.cancel()
    }
}
