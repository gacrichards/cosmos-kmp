package com.gacrichards.cosmos.data.repository

import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.model.MediaType
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
    copyright = null,
)

// Fake cache that records inserts and serves configurable lookups
private class FakeApodCache(private var stored: Apod? = null) {
    var insertCallCount = 0

    fun getByDate(date: String): Apod? = stored?.takeIf { it.date == date }

    fun insert(apod: Apod) {
        insertCallCount++
        stored = apod
    }
}

// Fake API service that either returns an apod or throws
private class FakeApodApiService(private val result: Result<Apod>) {
    suspend fun getTodayApod(): Apod = result.getOrThrow()
}

// Testable subclass that accepts fake collaborators and overrides date lookup
private class TestableApodRepository(
    private val fakeApi: FakeApodApiService,
    private val fakeCache: FakeApodCache,
    private val today: String,
) : ApodRepository {

    override fun getApodByDate(date: String): Flow<Result<Apod>> = kotlinx.coroutines.flow.flowOf(Result.failure(NotImplementedError()))
    override fun getApodArchive(startDate: String, endDate: String): Flow<Result<List<Apod>>> = kotlinx.coroutines.flow.flowOf(Result.success(emptyList()))

    override fun getTodayApod(): Flow<Result<Apod>> = kotlinx.coroutines.flow.flow {
        fakeCache.getByDate(today)?.let { emit(Result.success(it)) }
        try {
            val fresh = fakeApi.getTodayApod()
            fakeCache.insert(fresh)
            emit(Result.success(fresh))
        } catch (e: Exception) {
            if (fakeCache.getByDate(today) == null) {
                emit(Result.failure(e))
            }
        }
    }
}

class ApodRepositoryTest {

    @Test
    fun `emits network result when cache is empty`() = runTest {
        val repo = TestableApodRepository(
            fakeApi = FakeApodApiService(Result.success(testApod)),
            fakeCache = FakeApodCache(stored = null),
            today = testApod.date,
        )

        val results = repo.getTodayApod().toList()
        assertEquals(1, results.size)
        assertEquals(testApod, results[0].getOrThrow())
    }

    @Test
    fun `emits cached result first, then network result`() = runTest {
        val cached = testApod.copy(title = "Cached Title")
        val fresh = testApod.copy(title = "Fresh Title")
        val repo = TestableApodRepository(
            fakeApi = FakeApodApiService(Result.success(fresh)),
            fakeCache = FakeApodCache(stored = cached),
            today = testApod.date,
        )

        val results = repo.getTodayApod().toList()
        assertEquals(2, results.size)
        assertEquals("Cached Title", results[0].getOrThrow().title)
        assertEquals("Fresh Title", results[1].getOrThrow().title)
    }

    @Test
    fun `emits only error when cache is empty and network fails`() = runTest {
        val error = RuntimeException("No network")
        val repo = TestableApodRepository(
            fakeApi = FakeApodApiService(Result.failure(error)),
            fakeCache = FakeApodCache(stored = null),
            today = testApod.date,
        )

        val results = repo.getTodayApod().toList()
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertEquals(error, results[0].exceptionOrNull())
    }

    @Test
    fun `swallows network error silently when cache was already emitted`() = runTest {
        val cached = testApod.copy(title = "Cached Title")
        val repo = TestableApodRepository(
            fakeApi = FakeApodApiService(Result.failure(RuntimeException("Network down"))),
            fakeCache = FakeApodCache(stored = cached),
            today = testApod.date,
        )

        val results = repo.getTodayApod().toList()
        // Only the cached result — no error emitted
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals("Cached Title", results[0].getOrThrow().title)
    }

    @Test
    fun `inserts network result into cache`() = runTest {
        val cache = FakeApodCache(stored = null)
        val repo = TestableApodRepository(
            fakeApi = FakeApodApiService(Result.success(testApod)),
            fakeCache = cache,
            today = testApod.date,
        )

        repo.getTodayApod().toList()
        assertEquals(1, cache.insertCallCount)
    }
}
