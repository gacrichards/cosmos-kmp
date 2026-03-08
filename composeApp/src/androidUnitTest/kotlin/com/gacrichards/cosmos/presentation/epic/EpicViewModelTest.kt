package com.gacrichards.cosmos.presentation.epic

import app.cash.turbine.test
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.EpicImage
import com.gacrichards.cosmos.domain.repository.EpicRepository
import com.gacrichards.cosmos.domain.usecase.GetEpicAvailableDatesUseCase
import com.gacrichards.cosmos.domain.usecase.GetEpicImagesUseCase
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
import kotlin.test.assertIs

private val testDates = listOf("2024-01-15", "2024-01-14", "2024-01-13")

private val testImages = listOf(
    EpicImage(
        identifier = "20240115000000",
        date = "2024-01-15",
        imageName = "epic_1b_20240115000000",
        caption = "Earth from EPIC",
        thumbnailUrl = "https://epic.gsfc.nasa.gov/archive/natural/2024/01/15/thumbs/epic_1b_20240115000000.jpg",
        fullUrl = "https://epic.gsfc.nasa.gov/archive/natural/2024/01/15/png/epic_1b_20240115000000.png",
    )
)

private class FakeEpicRepository(
    private val imagesResult: Result<List<EpicImage>> = Result.success(testImages),
    private val datesResult: Result<List<String>> = Result.success(testDates),
) : EpicRepository {
    override fun getAvailableDates(): Flow<Result<List<String>>> = flowOf(datesResult)
    override fun getImages(date: String): Flow<Result<List<EpicImage>>> = flowOf(imagesResult)
}

@OptIn(ExperimentalCoroutinesApi::class)
class EpicViewModelTest {
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

    private fun viewModel(
        imagesResult: Result<List<EpicImage>> = Result.success(testImages),
        datesResult: Result<List<String>> = Result.success(testDates),
    ) = EpicViewModel(
        getImages = GetEpicImagesUseCase(FakeEpicRepository(imagesResult, datesResult)),
        getAvailableDates = GetEpicAvailableDatesUseCase(FakeEpicRepository(imagesResult, datesResult)),
    )

    @Test
    fun `initial state is Loading before coroutines run`() = runTest(testDispatcher) {
        val vm = viewModel()
        assertEquals(UiState.Loading, vm.uiState.value.images)
        assertEquals(emptyList(), vm.uiState.value.availableDates)
    }

    @Test
    fun `loads dates and images on init`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(testDates, vm.uiState.value.availableDates)
        assertEquals("2024-01-15", vm.uiState.value.currentDate)
        assertIs<UiState.Success<List<EpicImage>>>(vm.uiState.value.images)
    }

    @Test
    fun `emits Loading then Success after init`() = runTest(testDispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            assertEquals(UiState.Loading, awaitItem().images)
            advanceUntilIdle()
            var item = awaitItem()
            while (item.images is UiState.Loading) {
                item = awaitItem()
            }
            val success = assertIs<UiState.Success<List<EpicImage>>>(item.images)
            assertEquals(testImages, success.data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Error when dates fetch fails`() = runTest(testDispatcher) {
        val vm = viewModel(datesResult = Result.failure(RuntimeException("No network")))
        advanceUntilIdle()
        assertIs<UiState.Error>(vm.uiState.value.images)
        assertEquals("No network", (vm.uiState.value.images as UiState.Error).message)
    }

    @Test
    fun `PreviousDate increments index and loads images`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals("2024-01-15", vm.uiState.value.currentDate)

        vm.onEvent(EpicEvent.PreviousDate)
        advanceUntilIdle()
        assertEquals("2024-01-14", vm.uiState.value.currentDate)
    }

    @Test
    fun `NextDate decrements index and loads images`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        // Go back first
        vm.onEvent(EpicEvent.PreviousDate)
        advanceUntilIdle()
        assertEquals("2024-01-14", vm.uiState.value.currentDate)

        vm.onEvent(EpicEvent.NextDate)
        advanceUntilIdle()
        assertEquals("2024-01-15", vm.uiState.value.currentDate)
    }

    @Test
    fun `canGoPrevious is true when not at oldest date`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assert(vm.uiState.value.canGoPrevious)
    }

    @Test
    fun `canGoNext is false at most recent date`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assert(!vm.uiState.value.canGoNext)
    }

    @Test
    fun `Retry reloads images for current date`() = runTest(testDispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(EpicEvent.Retry)
            assertEquals(UiState.Loading, awaitItem().images)
            advanceUntilIdle()
            assertIs<UiState.Success<List<EpicImage>>>(awaitItem().images)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
