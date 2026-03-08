package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.model.EpicImage
import com.gacrichards.cosmos.domain.repository.EpicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
    private val datesResult: Result<List<String>> = Result.success(listOf("2024-01-15")),
) : EpicRepository {
    override fun getAvailableDates(): Flow<Result<List<String>>> = flowOf(datesResult)
    override fun getImages(date: String): Flow<Result<List<EpicImage>>> = flowOf(imagesResult)
}

class GetEpicImagesUseCaseTest {

    @Test
    fun `returns success result from repository`() = runTest {
        val useCase = GetEpicImagesUseCase(FakeEpicRepository())
        useCase("2024-01-15").collect { result ->
            assertIs<Result<List<EpicImage>>>(result)
            assertEquals(testImages, result.getOrNull())
        }
    }

    @Test
    fun `returns failure result from repository`() = runTest {
        val error = RuntimeException("Network error")
        val useCase = GetEpicImagesUseCase(FakeEpicRepository(imagesResult = Result.failure(error)))
        useCase("2024-01-15").collect { result ->
            assert(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
    }

    @Test
    fun `passes date parameter to repository`() = runTest {
        var capturedDate: String? = null
        val repo = object : EpicRepository {
            override fun getAvailableDates(): Flow<Result<List<String>>> = flowOf(Result.success(emptyList()))
            override fun getImages(date: String): Flow<Result<List<EpicImage>>> {
                capturedDate = date
                return flowOf(Result.success(emptyList()))
            }
        }
        GetEpicImagesUseCase(repo)("2024-03-08").collect {}
        assertEquals("2024-03-08", capturedDate)
    }
}
