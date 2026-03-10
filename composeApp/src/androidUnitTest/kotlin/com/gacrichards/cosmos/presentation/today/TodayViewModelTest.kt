package com.gacrichards.cosmos.presentation.today

import app.cash.turbine.test
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.model.MediaType
import com.gacrichards.cosmos.domain.repository.ApodRepository
import com.gacrichards.cosmos.domain.usecase.GetTodayApodUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val testApod = Apod(
    date = "2024-11-15",
    title = "NGC 1232",
    explanation = "A grand design spiral galaxy.",
    mediaType = MediaType.Image("https://apod.nasa.gov/apod/image/2411/ngc1232b_vlt_960.jpg"),
    hdUrl = null,
    copyright = null,
)

private class FakeApodRepository(private val result: Result<Apod>) : ApodRepository {
    override fun getTodayApod(): Flow<Result<Apod>> = flowOf(result)
    override fun getApodByDate(date: String): Flow<Result<Apod>> = flowOf(result)
    override fun getApodArchive(startDate: String, endDate: String): Flow<Result<List<Apod>>> = flowOf(Result.success(emptyList()))
}
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading before coroutines run`() = runTest(testDispatcher) {
        val viewModel = viewModel(Result.success(testApod))
        assertEquals(UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `emits Loading then Success on successful fetch`() = runTest(testDispatcher) {
        val viewModel = viewModel(Result.success(testApod))

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(UiState.Success(testApod), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Error on failed fetch`() = runTest(testDispatcher) {
        val viewModel = viewModel(Result.failure(RuntimeException("Network error")))

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(UiState.Error("Network error"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry resets to Loading then emits Success`() = runTest(testDispatcher) {
        val viewModel = viewModel(Result.success(testApod))
        advanceUntilIdle()
        assertEquals(UiState.Success(testApod), viewModel.uiState.value)

        viewModel.uiState.test {
            assertEquals(UiState.Success(testApod), awaitItem())

            viewModel.onEvent(TodayEvent.Retry)
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(UiState.Success(testApod), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(result: Result<Apod>) =
        TodayViewModel(GetTodayApodUseCase(FakeApodRepository(result)))
}
