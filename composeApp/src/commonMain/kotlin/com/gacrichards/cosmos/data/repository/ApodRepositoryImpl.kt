package com.gacrichards.cosmos.data.repository

import com.gacrichards.cosmos.data.local.ApodCache
import com.gacrichards.cosmos.data.remote.ApodApiService
import com.gacrichards.cosmos.data.remote.mapper.toDomain
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ApodRepositoryImpl(
    private val apiService: ApodApiService,
    private val cache: ApodCache,
) : ApodRepository {

    override fun getTodayApod(): Flow<Result<Apod>> = flow {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        // Emit cached value immediately if available
        cache.getByDate(today)?.let { emit(Result.success(it)) }

        // Fetch from network and update cache
        try {
            val fresh = apiService.getTodayApod().toDomain()
            cache.insert(fresh)
            emit(Result.success(fresh))
        } catch (e: Exception) {
            // Only emit error if we had nothing to show from cache
            if (cache.getByDate(today) == null) {
                emit(Result.failure(e))
            }
        }
    }
}
