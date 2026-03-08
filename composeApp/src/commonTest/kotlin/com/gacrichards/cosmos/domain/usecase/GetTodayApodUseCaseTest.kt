package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.model.MediaType
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val testApod = Apod(
    date = "2024-11-15",
    title = "NGC 1232",
    explanation = "A grand design spiral galaxy.",
    mediaType = MediaType.Image("https://apod.nasa.gov/apod/image/2411/ngc1232b_vlt_960.jpg"),
    hdUrl = null,
    copyright = "ESO",
)

private class FakeApodRepository(private val result: Result<Apod>) : ApodRepository {
    override fun getTodayApod(): Flow<Result<Apod>> = flowOf(result)
}

class GetTodayApodUseCaseTest {

    @Test
    fun `returns success result from repository`() = runTest {
        val useCase = GetTodayApodUseCase(FakeApodRepository(Result.success(testApod)))
        assertEquals(Result.success(testApod), useCase().first())
    }

    @Test
    fun `returns failure result from repository`() = runTest {
        val error = RuntimeException("Network error")
        val useCase = GetTodayApodUseCase(FakeApodRepository(Result.failure(error)))
        val result = useCase().first()
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
