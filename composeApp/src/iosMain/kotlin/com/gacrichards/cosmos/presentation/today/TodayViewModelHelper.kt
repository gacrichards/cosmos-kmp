package com.gacrichards.cosmos.presentation.today

import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class TodayViewModelHelper : KoinComponent {

    private val viewModel: TodayViewModel = get()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null

    fun startObserving(
        onLoading: () -> Unit,
        onSuccess: (Apod) -> Unit,
        onError: (String) -> Unit,
    ) {
        job = scope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> onLoading()
                    is UiState.Success -> onSuccess(state.data)
                    is UiState.Error -> onError(state.message)
                }
            }
        }
    }

    fun retry() = viewModel.onEvent(TodayEvent.Retry)

    fun dispose() {
        job?.cancel()
    }
}
